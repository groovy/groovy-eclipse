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
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;

public class ClosuresInCodePredicate implements IASTNodePredicate {

    @Override
    public ASTNode evaluate(ASTNode input) {
        if (input instanceof ClosureExpression) {
            ClosureExpression cl = (ClosureExpression) input;
            if (ASTTools.hasValidPosition(cl))
                return cl;
        }
        return null;
    }
}
