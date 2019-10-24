/*
 * Copyright 2009-2019 the original author or authors.
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

import java.util.Collection;
import java.util.stream.Collectors;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.core.resources.IStorage;

/**
 * the matches on the name of the object
 */
public class NamePointcut extends FilteringPointcut<Object> {

    public NamePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, Object.class);
    }

    @Override
    protected Collection<Object> explodeObject(Object toMatch) {
        Collection<Object> objects = super.explodeObject(toMatch);
        if (objects == null) {
            return null;
        }
        return objects.stream().map(object -> {
            if (object instanceof ClassNode) {
                return ((ClassNode) object).getName();
            } else if (object instanceof FieldNode) {
                return ((FieldNode) object).getName();
            } else if (object instanceof MethodNode) {
                return ((MethodNode) object).getName();
            } else if (object instanceof PropertyNode) {
                return ((PropertyNode) object).getName();
            } else if (object instanceof AnnotationNode) {
                return ((AnnotationNode) object).getClassNode().getName();
            } else if (object instanceof MethodCallExpression) {
                return ((MethodCallExpression) object).getMethodAsString();
            } else if (object instanceof MapEntryExpression) {
                return ((MapEntryExpression) object).getKeyExpression().getText();
            } else if (object instanceof Variable) {
                return ((Variable) object).getName();
            } else if (object instanceof BinaryExpression &&
                ((BinaryExpression) object).getLeftExpression() instanceof Variable &&
                ((BinaryExpression) object).getOperation().isA(Types.ASSIGNMENT_OPERATOR)) {
                return ((Variable) ((BinaryExpression) object).getLeftExpression()).getName();
            } else if (object instanceof Expression) {
                return ((Expression) object).getText();
            } else {
                return object.toString();
            }
        }).collect(Collectors.toList());
    }

    @Override
    protected Object filterObject(Object name, GroovyDSLDContext context, String firstArgAsString) {
        return (firstArgAsString == null || firstArgAsString.equals(name) ? name : null);
    }
}
