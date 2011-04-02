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
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
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

    public VariableExpressionPointcut(String containerIdentifier) {
        super(containerIdentifier, Expression.class);
    }


    /**
     * extracts fields from the outer binding, or from the current type if there is no outer binding
     * the outer binding should be either a {@link Collection} or a {@link ClassNode}
     */
    protected List<Expression> filterOuterBindingByType(GroovyDSLDContext pattern) {
        Object outer = pattern.getOuterPointcutBinding();
        if (outer == null) {
            ASTNode node = pattern.getCurrentScope().getCurrentNode();
            if (node instanceof VariableExpression || node instanceof ConstantExpression) {
                List<Expression> vars = new ArrayList<Expression>(1);
                vars.add((Expression) node);
                return vars;
            } else {
                return null;
            }
        } else {
            if (outer instanceof Collection<?>) {
                List<Expression> vars = new ArrayList<Expression>();
                for (Object elt : (Collection<Object>) outer) {
                    if (elt instanceof VariableExpression || elt instanceof ConstantExpression) {
                        vars.add((Expression) elt);
                    }
                }
                return vars;
            }
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
