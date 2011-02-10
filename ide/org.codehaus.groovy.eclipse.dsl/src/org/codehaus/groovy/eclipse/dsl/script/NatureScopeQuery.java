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
 * 
 * @author andrew
 * @created Nov 26, 2010
 */
public class NatureScopeQuery implements IContextQuery {

    private final String nature;
    
    public NatureScopeQuery(String nature) {
        this.nature = nature;
    }

    public IContextQueryResult<?> evaluate(AnnotatedNode node, VariableScope currentScope) {
        // return null for now since we are not keeping track of anything JDT inside the scope
        return null;
    }

}
