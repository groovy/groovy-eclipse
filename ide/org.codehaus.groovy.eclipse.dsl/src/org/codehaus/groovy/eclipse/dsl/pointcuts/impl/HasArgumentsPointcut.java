/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * Matches based on arguments to method calls.
 */
public class HasArgumentsPointcut extends FilteringPointcut<AnnotatedNode>  {

    public HasArgumentsPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, AnnotatedNode.class);
    }

    /**
     * Converts method call arguments or method decl parameters to expressions.
     */
    @Override
    protected Collection<AnnotatedNode> explodeObject(Object toMatch) {
        if (toMatch instanceof MethodCall) {
            Expression arguments = ((MethodCall) toMatch).getArguments();
            if (arguments instanceof TupleExpression) {
                Collection<Expression> innerArgs = ((TupleExpression) arguments).getExpressions();
                Collection<AnnotatedNode> actualArgs = new ArrayList<>(innerArgs.size());
                for (Expression innerArg : innerArgs) {
                    if (innerArg instanceof MapExpression) {
                        actualArgs.addAll(((MapExpression) innerArg).getMapEntryExpressions());
                    } else {
                        actualArgs.add(innerArg);
                    }
                }
                return actualArgs;
            } else if (arguments instanceof ListExpression) {
                return new ArrayList<>(((ListExpression) arguments).getExpressions());
            } else if (arguments instanceof MapExpression) {
                return new ArrayList<>(((MapExpression) arguments).getMapEntryExpressions());
            } else {
                return Collections.<AnnotatedNode>singleton(arguments);
            }
        } else if (toMatch instanceof MethodNode) {
            Parameter[] parameters = ((MethodNode) toMatch).getParameters();
            if (parameters != null) {
                return new ArrayList<>(Arrays.asList(parameters));
            }
        }
        return null;
    }

    @Override
    protected AnnotatedNode filterObject(AnnotatedNode result, GroovyDSLDContext context, String firstArgAsString) {
        boolean matches = false;
        if (firstArgAsString == null) {
            matches = true;
        } else if (result instanceof Variable) {
            matches = firstArgAsString.equals(((Variable) result).getName());
        } else if (result instanceof MapEntryExpression) {
            if (((MapEntryExpression) result).getKeyExpression() instanceof ConstantExpression) {
                matches = firstArgAsString.equals(((MapEntryExpression) result).getKeyExpression().getText());
            }
        }

        if (matches) {
            if (result instanceof MapEntryExpression) {
                return ((MapEntryExpression) result).getValueExpression();
            }
            return result; // parameter or variable
        }
        return null;
    }
}
