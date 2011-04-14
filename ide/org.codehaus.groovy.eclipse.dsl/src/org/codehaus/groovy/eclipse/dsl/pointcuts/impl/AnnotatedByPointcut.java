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

import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;

/**
 * the match returns true if the pattern passed in has an annotated node that has an annotation 
 * with the supplied characteristics most likely, a name.  This is similar to {@link FindAnnotationPointcut} on when it matches.
 * However, the difference is that the matched results are the things that are annotated, rather than the annotations 
 * 
 * @author andrew
 * @created Feb 11, 2011
 */
public class AnnotatedByPointcut extends FilteringPointcut<AnnotatedNode> {

    public AnnotatedByPointcut(String containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, AnnotatedNode.class);
    }
    
    protected AnnotatedNode filterObject(AnnotatedNode result, GroovyDSLDContext context, String firstArgAsString) {
        List<AnnotationNode> annotations = result.getAnnotations();
        if (annotations != null) {
            for (AnnotationNode annotation : annotations) {
                if (annotation.getClassNode().getName().equals(firstArgAsString)) {
                    return result;
                }
            }
        }
        return null;
    }

}
