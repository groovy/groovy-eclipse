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

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;

/**
 * the match returns true if the pattern passed in is a variable expression
 * that matches the contained argument
 * @author andrew
 * @created Feb 11, 2011
 */
public class VariableExpressionPointcut extends FilteringPointcut<Expression> {

    public VariableExpressionPointcut(String containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, Expression.class);
    }


    /**
     * Converts toMatch to a collection of expression nodes ({@link VariableExpression} or {@link ConstantExpression} nodes).  Might be null or empty list
     * In either of these cases, this is considered a non-match
     * @param toMatch the object to explode
     */
    @Override
    protected Collection<Expression> explodeObject(Object toMatch) {
        if (toMatch instanceof Collection<?>) {
            List<Expression> fields = new ArrayList<Expression>();
            for (Object elt : (Collection<?>) toMatch) {
                if (elt instanceof Expression) {
                    fields.add((Expression) elt);
                }
            }
            return fields;
        } else if (toMatch instanceof Expression) {
            return Collections.singleton((Expression) toMatch);
        }
        return null;
    }
    
    
    @Override
    protected Expression filterObject(Expression result, GroovyDSLDContext context, String firstArgAsString) {
        if (result instanceof VariableExpression || result instanceof ConstantExpression) {
            if (firstArgAsString == null || result.getText().equals(firstArgAsString)) {
                return result;
            }
        }
        return null;
    }
}
