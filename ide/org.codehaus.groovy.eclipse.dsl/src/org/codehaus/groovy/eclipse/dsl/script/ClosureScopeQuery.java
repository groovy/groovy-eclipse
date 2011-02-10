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
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * 
 * @author andrew
 * @created Nov 26, 2010
 */
public class ClosureScopeQuery implements IContextQuery {

    private final boolean isArg;
    
    public ClosureScopeQuery(boolean isArg) {
        this.isArg = isArg;
    }

    /**
     * Return 
     */
    public IContextQueryResult<?> evaluate(AnnotatedNode node, VariableScope currentScope) {
        if (currentScope.isTopLevel()) {
            // FIXADE find a better way of doing this.  Top level scopes should not care if they are in 
            // a closure
            return null;
        }
        ClosureExpression enclosingClosure = currentScope.getEnclosingClosure();
        if (enclosingClosure != null) {
            if (!isArg || (isArg && currentScope.getEnclosingMethodCallExpression() != null)) {
                return new SingleNodeResult<ClosureExpression>(enclosingClosure);
            }
        }
        return EmptyResult.INSTANCE;
    }
}
