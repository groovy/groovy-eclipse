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

    public FindAnnotationPointcut(String containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, AnnotationNode.class);
    }
    
    protected AnnotationNode filterObject(AnnotationNode result, GroovyDSLDContext context, String firstArgAsString) {
        if (result.getClassNode().getName().equals(firstArgAsString)) {
            return result;
        }
        return null;
    }
    
    /**
     * Convert the outer binding into a list of annotations.
     */
    protected List<AnnotationNode> filterOuterBindingByType(GroovyDSLDContext pattern) {
        Object outer = pattern.getOuterPointcutBinding();
        if (outer instanceof AnnotationNode) {
            return Collections.singletonList((AnnotationNode) outer);
        } else if (outer instanceof AnnotatedNode) {
            List<AnnotationNode> annotations = ((AnnotatedNode) outer).getAnnotations();
            return (List<AnnotationNode>) (annotations != null ? annotations : Collections.emptyList());
        } else if (outer instanceof List) {
            ArrayList<AnnotationNode> annotations = new ArrayList<AnnotationNode>();
            for (Object element : (List<?>) outer) {
                if (element instanceof AnnotationNode) {
                    annotations.add((AnnotationNode) element);
                } else if (element instanceof AnnotatedNode) {
                    List<AnnotationNode> eltAnnotations = ((AnnotatedNode) element).getAnnotations();
                    if (eltAnnotations != null) {
                        annotations.addAll(eltAnnotations);
                    }
                }
            }
            return annotations;
        } else {
            return null;
        }
    }


}
