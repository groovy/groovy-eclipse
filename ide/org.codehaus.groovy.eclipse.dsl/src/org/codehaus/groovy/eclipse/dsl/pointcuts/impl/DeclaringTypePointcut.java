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

import java.util.Collection;
import java.util.Collections;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;

/**
 * Matches based on the declaring type of a declaration
 * @author andrew
 * @created Jul 22, 2011
 */
public class DeclaringTypePointcut extends FilteringPointcut<ClassNode>  {

    public DeclaringTypePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, ClassNode.class);
    }
    
    
    /**
     * Grabs the explicit declaring type of the {@link AnnotatedNode}.  This will not return
     *  the expected value for {@link MethodCallExpression}s.
     */
    @Override
    protected Collection<ClassNode> explodeObject(Object toMatch) {
        if (toMatch instanceof AnnotatedNode) {
            return Collections.singleton(((AnnotatedNode) toMatch).getDeclaringClass());
        }
        return null;
    }

    /**
     * determine if the name of the specified class is the same as its argument
     */
    @Override
    protected ClassNode filterObject(ClassNode result, GroovyDSLDContext context, String firstArgAsString) {
        return result != null && result.getName().equals(firstArgAsString) ? result : null;
    }
}
