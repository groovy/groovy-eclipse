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
package org.eclipse.jdt.groovy.search;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;

/**
 * An extension to the {@link ITypeLookup} interface that specifies that a particular expression visit occurs in the context of a
 * static object expression. We use an extension interface because we do not want to break backwards compatibility with existing
 * third party implementors of {@link ITypeLookup}.
 *
 * @author Andrew Eisenberg
 * @created Jun 9, 2011
 */
public interface ITypeLookupExtension extends ITypeLookup {
    /**
     * Determine the type for an expression node.
     *
     * @param node the AST Node to determine the type for
     * @param scope the variable scope at this location
     * @param objectExpressionType if the parent of node is a {@link PropertyExpression}, then this value contains the type of
     *        {@link PropertyExpression#getObjectExpression()}, otherwise null
     * @param isStaticObjectExpression true iff the objectExpressionType is not null and it is referring to the static instance of
     *        the class declaration
     * @return the type for the node and confidence in that type, or null if cannot determine
     */
    TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType, boolean isStaticObjectExpression);

    /**
     * Determines the type inside of a BlockStatement
     */
    void lookupInBlock(BlockStatement node, VariableScope scope);
}
