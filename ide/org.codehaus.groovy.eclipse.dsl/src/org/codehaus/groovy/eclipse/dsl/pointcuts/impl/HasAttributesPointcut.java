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
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * Matches based on attributes in {@link AnnotationNode}s
 * @author andrew
 * @created Jul 22, 2011
 */
public class HasAttributesPointcut extends FilteringPointcut<Expression>  {

    public HasAttributesPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, Expression.class);
    }
    
    
    /**
     * Converts the method call arguments to expressions
     */
    @Override
    protected Collection<Expression> explodeObject(Object toMatch) {
        if (toMatch instanceof AnnotationNode) {
            Map<String, Expression> members = ((AnnotationNode) toMatch).getMembers();
            // wrap the member value pairs in a collection of MapEntrys
            Collection<Expression> expressions = new ArrayList<Expression>(members.size());
            for (Entry<String, Expression> entry : members.entrySet()) {
                expressions.add(new MapEntryExpression(new ConstantExpression(entry.getKey()), entry.getValue()));
            }
            return expressions;
        }
        return null;
    }

    /**
     * by default, matches on the names of named arguments (if a named expression), otherwise passes the arguments to contained pointcuts 
     */
    @Override
    protected Expression filterObject(Expression result, GroovyDSLDContext context, String firstArgAsString) {
        if (firstArgAsString == null) {
            // always match
            if (result instanceof MapEntryExpression) {
                return ((MapEntryExpression) result).getValueExpression();
            } else {
                return result;
            }
        } 
        if (result instanceof MapEntryExpression) {
            MapEntryExpression entry = (MapEntryExpression) result;
            if (entry.getKeyExpression() instanceof ConstantExpression) {
                String argName = entry.getKeyExpression().getText();
                if (argName.equals(firstArgAsString)) {
                    return entry.getValueExpression();
                }
            }
        }
        return null;
    }
}
