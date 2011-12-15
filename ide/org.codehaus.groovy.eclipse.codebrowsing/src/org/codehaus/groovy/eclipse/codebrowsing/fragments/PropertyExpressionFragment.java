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
import org.codehaus.groovy.eclipse.codebrowsing.selection.IsSameExpression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Assert;

/**
 * An {@link IASTFragment} that is a part of a binary expression
 * @author andrew
 * @created Jun 4, 2010
 */
public class PropertyExpressionFragment implements IASTFragment {

    private final ASTFragmentKind kind;
    private final Expression expression;

    private final IASTFragment next;

    PropertyExpressionFragment(ASTFragmentKind token, Expression expression, IASTFragment next) {
        Assert.isNotNull(next);
        this.kind = token;
        this.expression = expression;
        this.next = next;
    }

    public Expression getAssociatedExpression() {
        return expression;
    }

    public ASTNode getAssociatedNode() {
        return expression;
    }

    public int getEnd() {
        return getNext().getEnd();
    }

    public int getStart() {
        return expression.getStart();
    }

    public int getLength() {
        return getEnd() - getStart();
    }

    public int getTrimmedEnd(GroovyCompilationUnit unit) {
        return getNext().getTrimmedEnd(unit);
    }

    public int getTrimmedLength(GroovyCompilationUnit unit) {
        return getTrimmedEnd(unit) - getStart();
    }

    public IASTFragment getNext() {
        return next;
    }

    public boolean matches(IASTFragment other) {
        if (!(other instanceof PropertyExpressionFragment)) {
            return false;
        }

        PropertyExpressionFragment otherBinary = (PropertyExpressionFragment) other;
        return otherBinary.kind() == this.kind && new IsSameExpression().isSame(expression, otherBinary.getAssociatedExpression())
                && this.next.matches(otherBinary.getNext());
    }

    @Override
    public String toString() {
        return print(0);
    }

    public String print(int indentLvl) {
        return ASTFragmentFactory.spaces(indentLvl) + "(P) " + expression.toString() + "\n" + next.print(indentLvl + 1);
    }

    public int fragmentLength() {
        return 1 + next.fragmentLength();
    }

    public void accept(FragmentVisitor visitor) {
        if (visitor.previsit(this) && visitor.visit(this)) {
            next.accept(visitor);
        }
    }

    public ASTFragmentKind kind() {
        return kind;
    }

    /**
     * must match from the beginning of each fragment
     */
    public IASTFragment findMatchingSubFragment(IASTFragment other) {
        if (this.fragmentLength() < other.fragmentLength()) {
            return new EmptyASTFragment();
        }

        if (other.kind() == ASTFragmentKind.SIMPLE_EXPRESSION
                && new IsSameExpression().isSame(this.getAssociatedExpression(), other.getAssociatedExpression())) {
            return new SimpleExpressionASTFragment(expression);
        } else if (other.kind() == this.kind()) {
            PropertyExpressionFragment toMatchBinary = (PropertyExpressionFragment) other;
            if (new IsSameExpression().isSame(this.getAssociatedExpression(), toMatchBinary.getAssociatedExpression())) {
                IASTFragment result = this.getNext().findMatchingSubFragment(toMatchBinary.getNext());
                if (result.kind() == ASTFragmentKind.EMPTY) {
                    // the target expressions match, but not the remainder
                    // so there is no match
                    return new EmptyASTFragment();
                } else {
                    // remainder (or part of it) matches.
                    return new PropertyExpressionFragment(this.kind(), this.getAssociatedExpression(), result);
                }
            }
        }
        return new EmptyASTFragment();
    }

    /**
     * A fragment matches another if:
     * 1. either one is a simple expression and the simple expression matches OR
     * 2. Fragments are of the same kind AND
     * --a. associated nodes are the same (and arguments if both are method call
     * fragments)
     * --b. if next node is the same, then include the next node, but doesn't
     * have to be.
     *
     * @param other
     * @return
     */
    // IASTFragment internalFindMatchingSubFragment(IASTFragment thiz,
    // IASTFragment toMatch) {
    // // complicated if statement:
    // // check that:
    // // 1. kinds are not the same,
    // // 2. at least one is a simple expression
    // // 3. neither are method calls (because would need to check args as
    // // well)
    // if (toMatch.kind() != thiz.kind()
    // && (toMatch.kind() == ASTFragmentKind.SIMPLE_EXPRESSION || thiz.kind() ==
    // ASTFragmentKind.SIMPLE_EXPRESSION)
    // && toMatch.kind() != ASTFragmentKind.METHOD_CALL && thiz.kind() !=
    // ASTFragmentKind.METHOD_CALL) {
    // if (new IsSameExpression().isSame(thiz.getAssociatedNode(),
    // toMatch.getAssociatedNode())) {
    // return new SimpleExpressionASTFragment(thiz.getAssociatedNode());
    // }
    // } else if (thiz.kind() == ASTFragmentKind.PROPERTY || thiz.kind() ==
    // ASTFragmentKind.METHOD_POINTER) {
    // PropertyExpressionFragment thizBinary = (PropertyExpressionFragment)
    // thiz;
    // PropertyExpressionFragment toMatchBinary = (PropertyExpressionFragment)
    // toMatch;
    // if (new IsSameExpression().isSame(thiz.getAssociatedNode(),
    // toMatch.getAssociatedNode())) {
    // IASTFragment result =
    // internalFindMatchingSubFragment(thizBinary.getNext(),
    // toMatchBinary.getNext());
    // if (result.kind() == ASTFragmentKind.EMPTY) {
    // return new SimpleExpressionASTFragment(thiz.getAssociatedNode());
    // } else {
    // return new PropertyExpressionFragment(thiz.kind(),
    // thiz.getAssociatedNode(), result);
    // }
    // }
    // } else if (thiz.kind() == ASTFragmentKind.METHOD_CALL) {
    // MethodCallFragment thizCall = (MethodCallFragment) thiz;
    // MethodCallFragment toMatchCall = (MethodCallFragment) toMatch;
    // if (new IsSameExpression().isSame(thizCall.getAssociatedNode(),
    // toMatchCall.getAssociatedNode())
    // && new IsSameExpression().isSame(thizCall.getArguments(),
    // toMatchCall.getArguments())) {
    // // now check to see if we need to continue
    // if (thizCall.hasNext() && toMatchCall.hasNext()) {
    // IASTFragment result = internalFindMatchingSubFragment(thizCall.getNext(),
    // toMatchCall.getNext());
    // if (result.kind() == ASTFragmentKind.EMPTY) {
    // return new MethodCallFragment(thiz.getAssociatedNode(),
    // thizCall.getArguments());
    // } else {
    // return new MethodCallFragment(thiz.getAssociatedNode(),
    // thizCall.getArguments(), result);
    // }
    // }
    // }
    // }
    // return new EmptyASTFragment();
    // }
}
