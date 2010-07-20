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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.IASTNodePredicate;

/**
 *
 * @author kdvolder
 * @created 2010-07-05
 */
public class ListInCodePredicate implements IASTNodePredicate {

    public ASTNode evaluate(ASTNode input) {
        if (input.getClass() == ListExpression.class) {
            if (ASTTools.hasValidPosition(input))
                return input;
        }
        return null;
    }
}
