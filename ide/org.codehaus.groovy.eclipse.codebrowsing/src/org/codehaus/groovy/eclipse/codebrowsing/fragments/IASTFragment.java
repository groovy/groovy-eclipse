/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.fragments;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

/**
 * Represents a fragment of an AST. A fragment consists of a number of ASTNodes.
 * These nodes are lexically contiguous even if they are not siblings in the
 * tree.
 *
 * This class has support for matching
 */
public interface IASTFragment {

    /**
     * Property-based fragments can only match if their beginnings match
     * Binary expression-based fragments can match anywhere
     *
     * @return true iff other matches this fragment
     */
    boolean matches(IASTFragment other);

    /**
     * Returns a sub-fragment of this one that matches other.
     *
     * Note that the components of the returned fragment may be different.
     * The sub-fragment may end with {@link SimpleExpressionASTFragment} even
     * if it corresponds with a {@link PropertyExpressionFragment} in the
     * original.
     *
     * Also note that Property-based fragments can only match from the
     * beginning,
     * but binary expression-based fragments can match from anywhere.
     *
     * @param other the sub-fragment to match
     * @return a matching sub-fragment or {@link EmptyASTFragment} if there is
     *         no match
     */
    IASTFragment findMatchingSubFragment(IASTFragment other);

    /**
     * @return lexical start of this fragment
     */
    int getStart();

    /**
     * @return lexical end of this fragment
     */
    int getEnd();

    /**
     * @return lexical end of this fragment with any trailing whitespace removed
     */
    int getTrimmedEnd(GroovyCompilationUnit unit);

    /**
     * Convenience method for getEnd() - getStart()
     */
    int getLength();

    /**
     * Convenience method for getTrimmedLength(unit) - getStart()
     */
    int getTrimmedLength(GroovyCompilationUnit unit);

    /**
     * The associated expression is the parent node that contains all fragments. The
     * source location of the node may be larger than the start and length
     */
    Expression getAssociatedExpression();

    /**
     * The associated node is the parent node that contains all fragments. The
     * source location of the node may be larger than the start and length
     */
    ASTNode getAssociatedNode();

    /**
     * Produces debug-ready string
     * this.toString() is equivalent to this.print(0)
     *
     * @param indentLvl number of double spaces to indent the string by
     */
    String print(int indentLvl);

    /**
     * @return number of subcomponents of this fragment
     */
    int fragmentLength();

    /**
     * Part of the visitor pattern
     */
    void accept(FragmentVisitor visitor);

    ASTFragmentKind kind();
}
