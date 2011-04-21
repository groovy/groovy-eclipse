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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;

/**
 * the matches on the name of the object
 * @author andrew
 * @created Feb 11, 2011
 */
public class NamePointcut extends FilteringPointcut<Object> {

    public NamePointcut(String containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, Object.class);
    }
    
    @Override
    protected Object filterObject(Object result, GroovyDSLDContext context, String firstArgAsString) {
        String toCompare;
        if (result instanceof ClassNode) {
            toCompare = ((ClassNode) result).getName();
        } else if (result instanceof FieldNode) {
            toCompare = ((FieldNode) result).getName();
        } else if (result instanceof MethodNode) {
            toCompare = ((MethodNode) result).getName();
        } else if (result instanceof PropertyNode) {
            toCompare = ((PropertyNode) result).getName();
        } else {
            toCompare = String.valueOf(result.toString());
        }
        return toCompare.equals(firstArgAsString) ? result : null;
    }

}
