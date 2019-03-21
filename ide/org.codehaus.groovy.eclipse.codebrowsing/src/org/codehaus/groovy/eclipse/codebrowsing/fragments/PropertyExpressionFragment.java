/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.fragments;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codebrowsing.selection.IsSameExpression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Assert;

/**
 * An {@link IASTFragment} that is a part of a binary expression.
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

    @Override
    public Expression getAssociatedExpression() {
        return expression;
    }

    @Override
    public ASTNode getAssociatedNode() {
        return expression;
    }

    @Override
    public int getEnd() {
        return getNext().getEnd();
    }

    @Override
    public int getStart() {
        return expression.getStart();
    }

    @Override
    public int getLength() {
        return getEnd() - getStart();
    }

    @Override
    public int getTrimmedEnd(GroovyCompilationUnit unit) {
        return getNext().getTrimmedEnd(unit);
    }

    @Override
    public int getTrimmedLength(GroovyCompilationUnit unit) {
        return getTrimmedEnd(unit) - getStart();
    }

    public IASTFragment getNext() {
        return next;
    }

    @Override
    public boolean matches(IASTFragment other) {
        if (!(other instanceof PropertyExpressionFragment)) {
            return false;
        }

        PropertyExpressionFragment otherBinary = (PropertyExpressionFragment) other;
        return otherBinary.kind() == this.kind &&
            new IsSameExpression().isSame(expression, otherBinary.getAssociatedExpression()) &&
            this.next.matches(otherBinary.getNext());
    }

    @Override
    public String toString() {
        return print(0);
    }

    @Override
    public String print(int indentLvl) {
        return ASTFragmentFactory.spaces(indentLvl) + "(P) " + expression.toString() + "\n" + next.print(indentLvl + 1);
    }

    @Override
    public int fragmentLength() {
        return 1 + next.fragmentLength();
    }

    @Override
    public void accept(FragmentVisitor visitor) {
        if (visitor.previsit(this) && visitor.visit(this)) {
            next.accept(visitor);
        }
    }

    @Override
    public ASTFragmentKind kind() {
        return kind;
    }

    /**
     * must match from the beginning of each fragment
     */
    @Override
    public IASTFragment findMatchingSubFragment(IASTFragment other) {
        if (this.fragmentLength() < other.fragmentLength()) {
            return new EmptyASTFragment();
        }

        if (other.kind() == ASTFragmentKind.SIMPLE_EXPRESSION &&
            new IsSameExpression().isSame(this.getAssociatedExpression(), other.getAssociatedExpression())) {
            return new SimpleExpressionASTFragment(expression);
        } else if (other.kind() == this.kind()) {
            PropertyExpressionFragment toMatchBinary = (PropertyExpressionFragment) other;
            if (new IsSameExpression().isSame(this.getAssociatedExpression(),
                toMatchBinary.getAssociatedExpression())) {
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
}
