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
import org.codehaus.groovy.syntax.Token;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Assert;

/**
 * An {@link IASTFragment} that is a part of a binary expression
 *
 * @author andrew
 * @created Jun 4, 2010
 */
public class BinaryExpressionFragment implements IASTFragment {

    private final Token token;

    private final Expression expression;

    private final IASTFragment next;

    private int actualStartPosition;

    BinaryExpressionFragment(Token token, Expression expression, IASTFragment next) {
        Assert.isNotNull(next);
        this.token = token;
        this.expression = expression;
        this.next = next;
        this.actualStartPosition = expression.getStart();
    }

    /**
     * Ensure that fragments that are the LHS of a declaration expression have t
     * the correct start position
     *
     * @param actualStartPosition the actual start of the fragment (include the
     *            'def' keyword or type)
     */
    void setActualStartPosition(int actualStartPosition) {
        this.actualStartPosition = actualStartPosition;
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
        return actualStartPosition;
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

    public Token getToken() {
        return token;
    }

    public IASTFragment getNext() {
        return next;
    }

    /**
     * @return true iff this fragment completely matches other
     */
    public boolean matches(IASTFragment other) {
        if (!(other instanceof BinaryExpressionFragment)) {
            return false;
        }

        BinaryExpressionFragment otherBinary = (BinaryExpressionFragment) other;
        return otherBinary.getToken().getText().equals(this.token.getText())
                && new IsSameExpression().isSame(expression, otherBinary.getAssociatedExpression())
                && this.next.matches(otherBinary.getNext());
    }

    @Override
    public String toString() {
        return print(0);
    }

    public String print(int indentLvl) {
        return ASTFragmentFactory.spaces(indentLvl) + "(B) " + expression.toString() + "\n" + next.print(indentLvl + 1);
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
        return ASTFragmentKind.BINARY;
    }

    /**
     * Finds a subfragment starting at this fragment that matches other.
     * Returns an empty fragment if no match.
     *
     * There may be further matches inside this one, but this method only loos
     * for fragments
     * that start at the beginning
     */
    public IASTFragment findMatchingSubFragment(IASTFragment other) {
        if (this.fragmentLength() < other.fragmentLength()) {
            return new EmptyASTFragment();
        }

        if (other.kind() == ASTFragmentKind.SIMPLE_EXPRESSION
                && new IsSameExpression().isSame(this.getAssociatedExpression(), other.getAssociatedExpression())) {
            return new SimpleExpressionASTFragment(expression);
        } else if (other.kind() == ASTFragmentKind.BINARY) {
            BinaryExpressionFragment otherBinary = (BinaryExpressionFragment) other;
            if (new IsSameExpression().isSame(this.getAssociatedExpression(), other.getAssociatedExpression())
                    && this.getToken().getText().equals(otherBinary.getToken().getText())) {
                // current component of the fragment matches. Now check to see
                // if there are any more pieces that match
                IASTFragment result = this.next.findMatchingSubFragment(otherBinary.next);
                if (result.kind() == ASTFragmentKind.EMPTY) {
                    // no more pieces match, so just return a simple expression
                    return new EmptyASTFragment();
                } else {
                    // more matches, include that in the sub-fragment
                    BinaryExpressionFragment newResult = new BinaryExpressionFragment(token, expression, result);
                    newResult.setActualStartPosition(actualStartPosition);
                    return newResult;
                }
            }
        }
        // current component does not match.
        return new EmptyASTFragment();
    }
}
