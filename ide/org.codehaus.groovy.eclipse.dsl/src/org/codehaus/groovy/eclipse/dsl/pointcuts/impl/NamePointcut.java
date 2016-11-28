/*
 * Copyright 2009-2016 the original author or authors.
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

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * the matches on the name of the object
 * @author andrew
 * @created Feb 11, 2011
 */
public class NamePointcut extends FilteringPointcut<Object> {

    public NamePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, Object.class);
    }
    
    @Override
    protected Object filterObject(Object result, GroovyDSLDContext context, String firstArgAsString) {
        String toCompare;
        if (result instanceof ClassNode) {
            toCompare = ((ClassNode) result).getName();
        } else if (result instanceof AnnotationNode) {
            toCompare = ((AnnotationNode) result).getClassNode().getName();
        } else if (result instanceof FieldNode) {
            toCompare = ((FieldNode) result).getName();
        } else if (result instanceof MethodNode) {
            toCompare = ((MethodNode) result).getName();
        } else if (result instanceof PropertyNode) {
            toCompare = ((PropertyNode) result).getName();
        } else if (result instanceof MapEntryExpression) {
            // argument to a method call
            toCompare = ((MapEntryExpression) result).getKeyExpression().getText();
        } else if (result instanceof MethodCallExpression) {
            toCompare = ((MethodCallExpression) result).getMethodAsString();
        } else if (result instanceof Variable) {
            toCompare = ((Variable) result).getName();
        } else if (result instanceof Expression) {
            toCompare = ((Expression) result).getText();
        } else {
            toCompare = String.valueOf(result.toString());
        }
        
        if (firstArgAsString == null) {
            // always match
            return toCompare;
        }
        return toCompare.equals(firstArgAsString) ? toCompare : null;
    }

}
