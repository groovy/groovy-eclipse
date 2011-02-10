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
package org.codehaus.groovy.eclipse.dsl.script;

import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.eclipse.jdt.groovy.search.VariableScope;

class HasAnnotationQuery implements IContextQuery {

    private final String name;
    
    public HasAnnotationQuery(String name) {
        this.name = name;
    }

    public IContextQueryResult<?> evaluate(AnnotatedNode node, VariableScope currentScope) {
        if (name != null) {
            @SuppressWarnings("cast") // keep cast so safe for 1.6
            List<AnnotationNode> anns = (List<AnnotationNode>) node.getAnnotations();
            if (anns != null) {
                for (AnnotationNode ann : anns) {
                    if (ann.getClassNode().getName().matches(name)) {
                        return new SingleNodeResult<AnnotatedNode>(node);
                    }
                }
            }
        }
        return EmptyResult.INSTANCE;
    }
}