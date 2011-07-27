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
        if (toMatch instanceof Collection<?>) {
            List<AnnotationNode> annotations = new ArrayList<AnnotationNode>();
            for (Object elt : (Collection<?>) toMatch) {
                Collection<AnnotationNode> explodedElt = explodeObject(elt);
                if (explodedElt != null) {
                    annotations.addAll(explodedElt);
                }
            }
            return annotations;
        } else if (toMatch instanceof AnnotatedNode) {
            return ((AnnotatedNode) toMatch).getAnnotations();
        } else if (toMatch instanceof AnnotationNode) {
            return Collections.singleton((AnnotationNode) toMatch);
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
