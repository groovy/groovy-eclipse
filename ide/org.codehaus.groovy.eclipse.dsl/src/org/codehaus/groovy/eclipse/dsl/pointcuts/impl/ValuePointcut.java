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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * matches on the value of an expression AST node.  Used inside of hasArguments and hasAttributes (inside of method calls and annotations respecively)
 * @author andrew
 * @created Feb 11, 2011
 */
public class ValuePointcut extends FilteringPointcut<Object> {

    public ValuePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, Object.class);
    }
    
    @Override
    protected Object filterObject(Object result, GroovyDSLDContext context, String firstArgAsString) {
        if (firstArgAsString == null) {
            return reify(result);
        }
        String toCompare;
        if (result instanceof ClassNode) {
            toCompare = ((ClassNode) result).getName();
        } else if (result instanceof FieldNode) {
            toCompare = ((FieldNode) result).getName();
        } else if (result instanceof MethodNode) {
            toCompare = ((MethodNode) result).getName();
        } else if (result instanceof PropertyNode) {
            toCompare = ((PropertyNode) result).getName();
        } else if (result instanceof Expression) {
            toCompare = ((Expression) result).getText();
        } else {
            toCompare = String.valueOf(result.toString());
        }
        return toCompare.equals(firstArgAsString) ? reify(result) : null;
    }


    /**
     * Attempt to convert this object (presumably an AST Node into a value in this compiler world
     * @param result
     * @return
     */
    private Object reify(Object result) {
        if (result instanceof MapEntryExpression) {
            return reify(((MapEntryExpression) result).getValueExpression());
        } else if (result instanceof ConstantExpression) {
            return ((ConstantExpression) result).getValue();
        } else if (result instanceof PropertyExpression) {
            PropertyExpression prop = (PropertyExpression) result;
            return reify(prop.getObjectExpression()).toString() + '.' + reify(prop.getProperty());
        } else {
            return super.asString(result);
        }
    }
}
