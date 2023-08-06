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

import java.util.Collection;
import java.util.Collections;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.core.resources.IStorage;

/**
 * Matches based on arguments to method calls.
 */
public class TypePointcut extends FilteringPointcut<ClassNode>  {

    public TypePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, ClassNode.class);
    }

    @Override
    protected Collection<ClassNode> explodeObject(Object toMatch) {
        ClassNode type = null;
        if (toMatch instanceof ClassNode) {
            type = (ClassNode) toMatch;
        } else if (toMatch instanceof FieldNode) {
            type = ((FieldNode) toMatch).getType();
        } else if (toMatch instanceof MethodNode) {
            type = ((MethodNode) toMatch).getReturnType();
        } else if (toMatch instanceof PropertyNode) {
            type = ((PropertyNode) toMatch).getType();
        } else if (toMatch instanceof Variable) {
            type = ((Variable) toMatch).getType();
        } else if (toMatch instanceof BinaryExpression &&
            ((BinaryExpression) toMatch).getLeftExpression() instanceof Variable &&
            ((BinaryExpression) toMatch).getOperation().isA(Types.ASSIGNMENT_OPERATOR)) {
            type = ((Variable) ((BinaryExpression) toMatch).getLeftExpression()).getType();
        } else if (toMatch instanceof Expression) {
            type = ((Expression) toMatch).getType();
        }

        if (type != null) {
            return Collections.singleton(type);
        } else {
            return null;
        }
    }

    @Override
    protected ClassNode filterObject(ClassNode result, GroovyDSLDContext context, String firstArgAsString) {
        return (firstArgAsString == null || firstArgAsString.equals(result.getName()) ? result : null);
    }
}
