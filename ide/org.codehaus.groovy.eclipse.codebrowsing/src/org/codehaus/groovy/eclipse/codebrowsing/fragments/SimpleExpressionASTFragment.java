/*
 * Copyright 2009-2017 the original author or authors.
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

public class SimpleExpressionASTFragment implements IASTFragment {

    private final Expression expression;

    private int actualStartPosition;

    SimpleExpressionASTFragment(Expression expression) {
        this.expression = expression;
        actualStartPosition = expression.getStart();
    }

    @Override
    public Expression getAssociatedExpression() {
        return expression;
    }

    @Override
    public ASTNode getAssociatedNode() {
        return expression;
    }

    void setActualStartPosition(int actualStartPosition) {
        this.actualStartPosition = actualStartPosition;
    }

    @Override
    public int getEnd() {
        return expression.getEnd();
    }

    @Override
    public int getStart() {
        return actualStartPosition;
    }

    @Override
    public int getLength() {
        return getEnd() - getStart();
    }

    @Override
    public int getTrimmedEnd(GroovyCompilationUnit unit) {
        char[] contents = unit.getContents();
        int end = Math.min(getEnd(), contents.length - 1);
        while (end > actualStartPosition && Character.isWhitespace(contents[end])) {
            end--;
        }
        return end;
    }

    @Override
    public int getTrimmedLength(GroovyCompilationUnit unit) {
        return getTrimmedEnd(unit) - getStart();
    }

    @Override
    public boolean matches(IASTFragment other) {
        if (!(other instanceof SimpleExpressionASTFragment)) {
            return false;
        }
        return new IsSameExpression().isSame(expression, other.getAssociatedExpression());
    }

    @Override
    public String toString() {
        return print(0);
    }

    @Override
    public String print(int indentLvl) {
        return ASTFragmentFactory.spaces(indentLvl) + "(S) " + expression.toString();
    }

    @Override
    public int fragmentLength() {
        return 1;
    }

    @Override
    public void accept(FragmentVisitor visitor) {
        visitor.previsit(this);
        visitor.visit(this);
    }

    @Override
    public ASTFragmentKind kind() {
        return ASTFragmentKind.SIMPLE_EXPRESSION;
    }

    @Override
    public IASTFragment findMatchingSubFragment(IASTFragment other) {
        // cannot match method call fragments since there are no arguments here
        if (other.kind() == ASTFragmentKind.SIMPLE_EXPRESSION &&
            new IsSameExpression().isSame(this.getAssociatedExpression(), other.getAssociatedExpression())) {
            return new SimpleExpressionASTFragment(expression);
        } else {
            return new EmptyASTFragment();
        }
    }
}
