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

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * the match returns true if the pattern passed in has an annotated node that has an annotation
 * with the supplied characteristics most likely, a name.  This is similar to {@link AnnotatedByPointcut} on when it matches.
 * However, the difference is that the matched results are the annotations themselves rather than the things they
 * are annotating
 *
 * @author andrew
 * @created Apr 14, 2011
 */
public class FindAnnotationPointcut extends FilteringPointcut<AnnotationNode> {

    public FindAnnotationPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, AnnotationNode.class);
    }

//    FIXADE pointcut with two hasAttributes is not working

    /**
     * Converts toMatch to a collection of annotation nodes.  Might be null or empty list
     * In either of these cases, this is considered a non-match
     * @param toMatch the object to explode
     */
    @Override
    protected Collection<AnnotationNode> explodeObject(Object toMatch) {
        if (toMatch instanceof Collection) {
            Collection<AnnotationNode> annotations = new ArrayList<AnnotationNode>();
            for (Object obj : (Collection<?>) toMatch) {
                Collection<AnnotationNode> explodedElt = explodeObject(obj);
                if (explodedElt != null) {
                    annotations.addAll(explodedElt);
                }
            }
            return annotations;
        } else if (toMatch instanceof AnnotationNode) {
            return Collections.singleton((AnnotationNode) toMatch);
        } else if (toMatch instanceof AnnotatedNode) {
            return new ArrayList<AnnotationNode>(((AnnotatedNode) toMatch).getAnnotations());
        }
        return null;
    }

    /**
     * Matches if the annotation has the class name of that is passed in
     */
    protected AnnotationNode filterObject(AnnotationNode result, GroovyDSLDContext context, String firstArgAsString) {
        if (result.getClassNode().getName().equals(firstArgAsString)) {
            return result;
        }
        return null;
    }
}
