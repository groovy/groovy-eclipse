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
import java.util.Collection;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * Matches any {@code AnnotatedNode} that has an annotation with the supplied
 * characteristics. <p> Example: {@code annotatedBy(SuppressWarnings.class)}
 */
public class FindAnnotationPointcut extends FilteringPointcut<AnnotationNode> {

    public FindAnnotationPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, AnnotationNode.class);
    }

    @Override
    protected Collection<AnnotationNode> explodeObject(Object object) {
        Collection<AnnotationNode> result = new ArrayList<>();
        explodeObject(object, result);
        return result;
    }

    protected void explodeObject(Object object, Collection<AnnotationNode> result) {
        if (object instanceof AnnotationNode) {
            AnnotationNode annotation = (AnnotationNode) object;
            if (isCollectorAnnotation(annotation)) {
                Expression expression = annotation.getMember("value");
                if (expression instanceof ListExpression) {
                    List<Expression> expressions =
                        ((ListExpression) expression).getExpressions();
                    explodeObject(expressions, result);
                } else {
                    explodeObject(expression, result);
                }
            }
            result.add(annotation);
        } else if (object instanceof AnnotationConstantExpression) {
            explodeObject(((ConstantExpression) object).getValue(), result);
        } else if (object instanceof AnnotatedNode) {
            explodeObject(((AnnotatedNode) object).getAnnotations(), result);
        } else if (object instanceof Collection) {
            ((Collection<?>) object).forEach(item -> explodeObject(item, result));
        }
    }

    @Override
    protected AnnotationNode filterObject(AnnotationNode result, GroovyDSLDContext context, String firstArgAsString) {
        if (firstArgAsString.equals(result.getClassNode().getName())) {
            return result;
        }
        return null;
    }

    protected static boolean isCollectorAnnotation(AnnotationNode annotation) {
        List<MethodNode> methods = annotation.getClassNode().getMethods();
        if (methods.size() == 1 && methods.get(0).getName().equals("value")) {
            ClassNode returnType = methods.get(0).getReturnType();
            if (returnType.isArray()) {
                return returnType.getComponentType().implementsInterface(ClassHelper.Annotation_TYPE);
            }
        }
        return false;
    }
}
