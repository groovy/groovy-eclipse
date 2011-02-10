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

import org.codehaus.groovy.ast.AnnotatedNode;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * Represents a query that makes up a part of a context.
 * Tests to see if the {@link AnnotatedNode} that is passed in 
 * matches some sort of query.  If there is a match, then
 * either the single matched node is returned, or an array of all
 * the matched nodes are returned.  DO NOT return a list.
 * @author andrew
 * @created Nov 19, 2010
 */
public interface IContextQuery {
    /**
     * @param node the node to test
     * @param currentScope TODO
     * @return null if the evaluation fails or the successful match if passes
     * 
     */
    IContextQueryResult<?> evaluate(AnnotatedNode node, VariableScope currentScope);
}
