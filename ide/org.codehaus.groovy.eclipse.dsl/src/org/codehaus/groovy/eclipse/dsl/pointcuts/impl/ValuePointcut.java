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
     * Attempts to convert this object (presumably an AST Node into a value in this compiler world.
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
