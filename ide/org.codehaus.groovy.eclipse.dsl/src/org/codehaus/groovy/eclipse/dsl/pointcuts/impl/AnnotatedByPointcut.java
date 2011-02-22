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

import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;

/**
 * the match returns true if the pattern passed in has an annotated node that has an annotation 
 * with the supplied characteristics most likely, a name
 * @author andrew
 * @created Feb 11, 2011
 */
public class AnnotatedByPointcut extends FilteringPointcut<AnnotatedNode> {

    public AnnotatedByPointcut(String containerIdentifier) {
        super(containerIdentifier, AnnotatedNode.class);
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
