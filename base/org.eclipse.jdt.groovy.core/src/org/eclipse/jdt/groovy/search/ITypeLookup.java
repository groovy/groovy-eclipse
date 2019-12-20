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
package org.eclipse.jdt.groovy.search;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

/**
 * Instances of this interface will attempt to determine the type of a Groovy ASTNode that is passed in.
 * <p>
 * Type Lookups are meant to be stateless. All interesting state should be stored in the variable lookup
 */
public interface ITypeLookup {

    /**
     * Performs initialization for this lookup. Primary use is to add proper default variables in the variable scope.
     *
     * @param unit
     * @param topLevelScope
     */
    default void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
    }

    /**
     * Determine the type for an expression node.
     *
     * @param node the AST Node to determine the type for
     * @param scope the variable scope at this location
     * @param objectExpressionType if the parent of node is a ClassExpression,
     *     PropertyExpression, AttributeExpression, MethodPointerExpression, or
     *     the like then this value contains the type of the left-hand expression
     * @return the type for the node and confidence in that type, or null if cannot determine
     */
    TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType);

    /**
     * Determine the type for a class node. Implementors should return null
     * unless the type returned should be different from the type passed in.
     *
     * @param node the AST Node to determine the type for
     * @param scope the variable scope available at this location
     * @return the type for the node and confidence in that type, or null if cannot determine
     */
    default TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
        return null;
    }

    /**
     * Determine the type for a field node.
     *
     * @param node the AST Node to determine the type for
     * @param scope the variable scope at this location
     * @return the type for the node and confidence in that type, or null if cannot determine
     */
    default TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
        return null;
    }

    /**
     * Determine the type for a method node.
     *
     * @param node the AST Node to determine the type for
     * @param scope the variable scope available at this location
     * @return the type for the node and confidence in that type, or null if cannot determine
     */
    default TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
        return null;
    }

    /**
     * Determine the type for a parameter node.
     *
     * @param node the AST Node to determine the type for
     * @param scope the variable scope available at this location
     * @return the type for the node and confidence in that type, or null if cannot determine
     */
    default TypeLookupResult lookupType(Parameter node, VariableScope scope) {
        return null;
    }
}
