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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * Matches based on arguments to method calls
 * @author andrew
 * @created Jul 22, 2011
 */
public class HasArgumentsPointcut extends FilteringPointcut<AnnotatedNode>  {

    public HasArgumentsPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, AnnotatedNode.class);
    }
    
    
    /**
     * Converts the method call arguments to expressions
     */
    @Override
    protected Collection<AnnotatedNode> explodeObject(Object toMatch) {
        if (toMatch instanceof MethodCallExpression) {
            Expression arguments = ((MethodCallExpression) toMatch).getArguments();
            if (arguments instanceof TupleExpression) {
                List<Expression> innerArgs = ((TupleExpression) arguments).getExpressions();
                List<AnnotatedNode> actualArgs = new ArrayList<AnnotatedNode>(innerArgs.size());
                for (Expression innerArg : innerArgs) {
                    if (innerArg instanceof MapExpression) {
                        actualArgs.addAll(((MapExpression) innerArg).getMapEntryExpressions());
                    } else {
                        actualArgs.add(innerArg);
                    }
                }
                return actualArgs;
            } else if (arguments instanceof ListExpression) {
                return new ArrayList<AnnotatedNode>(((ListExpression) arguments).getExpressions());
            } else if (arguments instanceof MapExpression) {
                List<MapEntryExpression> mapEntryExpressions = ((MapExpression) arguments).getMapEntryExpressions();
                List<AnnotatedNode> result = new ArrayList<AnnotatedNode>(mapEntryExpressions);
                result.addAll(mapEntryExpressions);
                return result;
            } else {
                return Collections.<AnnotatedNode>singleton(arguments);
            }
        } else if (toMatch instanceof MethodNode) {
        	Parameter[] parameters = ((MethodNode) toMatch).getParameters();
        	if (parameters != null) {
        		return Arrays.<AnnotatedNode>asList(parameters);
        	}
        }
        return null;
    }

    /**
     * by default, matches on the names of named arguments (if a named expression), otherwise passes the arguments to contained pointcuts 
     */
    @Override
    protected AnnotatedNode filterObject(AnnotatedNode result, GroovyDSLDContext context, String firstArgAsString) {
        if (firstArgAsString == null) {
            // always match
            if (result instanceof MapEntryExpression) {
                return ((MapEntryExpression) result).getValueExpression();
            } else {
                return result;
            }
        } else if (result instanceof MapEntryExpression) {
            MapEntryExpression entry = (MapEntryExpression) result;
            if (entry.getKeyExpression() instanceof ConstantExpression) {
                String argName = entry.getKeyExpression().getText();
                if (argName.equals(firstArgAsString)) {
                    return entry.getValueExpression();
                }
            }
        } else if (result instanceof Parameter) {
            String name = ((Parameter) result).getName();
            if (name.equals(firstArgAsString)) {
            	return result;
            }
        }
        return null;
    }
}
