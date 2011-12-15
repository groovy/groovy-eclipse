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
package org.codehaus.groovy.eclipse.codebrowsing.fragments;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Represents a fragment of an AST. A fragment consists of a number of ASTNodes.
 * These nodes are lexically contiguous even if they are not siblings in the
 * tree.
 *
 * This class has support for matching
 *
 * @author andrew
 * @created Jun 4, 2010
 */
public interface IASTFragment {

    /**
     * Property-based fragments can only match if their beginnings match
     * Binary expression-based fragments can match anywhere
     *
     * @param other
     * @return true iff other matches this fragment
     */
    public boolean matches(IASTFragment other);

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
    public IASTFragment findMatchingSubFragment(IASTFragment other);

    /**
     * @return lexical start of this fragment
     */
    public int getStart();

    /**
     * @return lexical end of this fragment
     */
    public int getEnd();

    /**
     * @return lexical end of this fragment with any trailing whitespace removed
     */
    public int getTrimmedEnd(GroovyCompilationUnit unit);
    
    /**
     * Convenience method for getEnd() - getStart()
     *
     * @return
     */
    public int getLength();

    /**
     * Convenience method for getTrimmedLength(unit) - getStart()
     *
     * @return
     */
    public int getTrimmedLength(GroovyCompilationUnit unit);
    
    /**
     * The associated node is the parent node that contains all fragments. The
     * source location
     * of the node may be larger than the start and length
     *
     * @return Will return null for {@link EnclosingASTNodeFragment}
     */
    public Expression getAssociatedExpression();

    /**
     * The associated node is the parent node that contains all fragments. The
     * source location
     * of the node may be larger than the start and length
     */
    public ASTNode getAssociatedNode();

    /**
     * Produces debug-ready string
     * this.toString() is equivalent to this.print(0)
     *
     * @param indentLvl number of double spaces to indent the string by
     */
    public String print(int indentLvl);

    /**
     * @return number of subcomponents of this fragment
     */
    public int fragmentLength();

    /**
     * Part of the visitor pattern
     */
    public void accept(FragmentVisitor visitor);

    public ASTFragmentKind kind();

}
