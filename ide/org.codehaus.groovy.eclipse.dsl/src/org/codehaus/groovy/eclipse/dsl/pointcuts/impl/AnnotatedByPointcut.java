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
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;

/**
 * the match returns true if the pattern passed in has an annotated node that has an annotation 
 * with the supplied characteristics most likely, a name
 * @author andrew
 * @created Feb 11, 2011
 */
public class AnnotatedByPointcut extends AbstractPointcut {

    public AnnotatedByPointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    public BindingSet matches(GroovyDSLDContext pattern) {
        List<AnnotatedNode> annotatables = getAnnotatables(pattern);
        if (annotatables == null || annotatables.size() == 0) {
            return null;
        }
        
        Object first = getFirstArgument();
        if (first instanceof String) {
            List<AnnotatedNode> matches = new ArrayList<AnnotatedNode>();
            for (AnnotatedNode annotatable : annotatables) {
                List<AnnotationNode> annotations = annotatable.getAnnotations();
                if (annotations != null) {
                    for (AnnotationNode annotation : annotations) {
                        if (annotation.getClassNode().getName().equals(first)) {
                            matches.add(annotatable);
                            break;
                        }
                    }
                }
            }
            if (matches.size() == 0) {
                return null;
            } else if (matches.size() == 1) {
                return new BindingSet(matches.get(0));
            } else {
                return new BindingSet(matches);
            }
        } else {
            pattern.setOuterPointcutBinding(annotatables);
            return ((IPointcut) first).matches(pattern);
        }
    }

    /**
     * extracts annotated nodes from the outer binding, or from the current type if there is no outer binding
     * the outer binding should be either a {@link Collection} or a {@link ClassNode}
     */
    private List<AnnotatedNode> getAnnotatables(GroovyDSLDContext pattern) {
        Object outer = pattern.getOuterPointcutBinding();
        if (outer == null) {
            return Collections.singletonList((AnnotatedNode) pattern.getCurrentType());
        } else {
            if (outer instanceof Collection<?>) {
                List<AnnotatedNode> annotatables = new ArrayList<AnnotatedNode>();
                for (Object elt : (Collection<Object>) outer) {
                    if (elt instanceof AnnotatedNode) {
                        annotatables.add((AnnotatedNode) elt);
                    }
                }
                return annotatables;
            } else if (outer instanceof AnnotatedNode) {
                return Collections.singletonList((AnnotatedNode) outer);
            }
        }
        return null;
    }

    /**
     * Expecting one argument that can either be a string or another pointcut
     */
    @Override
    public String verify() {
        String oneStringOrOnePointcutArg = oneStringOrOnePointcutArg();
        if (oneStringOrOnePointcutArg == null) {
            return super.verify();
        }
        return oneStringOrOnePointcutArg;
    }
}
