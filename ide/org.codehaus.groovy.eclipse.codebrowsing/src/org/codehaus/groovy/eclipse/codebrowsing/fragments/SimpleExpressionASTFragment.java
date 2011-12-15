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

/**
 *
 * @author andrew
 * @created Jun 4, 2010
 */
public class SimpleExpressionASTFragment implements IASTFragment {

    private final Expression expression;

    private int actualStartPosition;

    SimpleExpressionASTFragment(Expression expression) {
        this.expression = expression;
        actualStartPosition = expression.getStart();
    }

    public Expression getAssociatedExpression() {
        return expression;
    }

    public ASTNode getAssociatedNode() {
        return expression;
    }

    void setActualStartPosition(int actualStartPosition) {
        this.actualStartPosition = actualStartPosition;
    }

    public int getEnd() {
        return expression.getEnd();
    }

    public int getStart() {
        return actualStartPosition;
    }

    public int getLength() {
        return getEnd() - getStart();
    }

    public int getTrimmedEnd(GroovyCompilationUnit unit) {
        char[] contents = unit.getContents();
        int end = getEnd();
        while (end > actualStartPosition && Character.isWhitespace(contents[end])) {
            end--;
        }
        return end;
    }

    public int getTrimmedLength(GroovyCompilationUnit unit) {
        return getTrimmedEnd(unit) - getStart();
    }

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

    public String print(int indentLvl) {
        return ASTFragmentFactory.spaces(indentLvl) + "(S) " + expression.toString();
    }

    public int fragmentLength() {
        return 1;
    }

    public void accept(FragmentVisitor visitor) {
        visitor.previsit(this);
        visitor.visit(this);
    }

    public ASTFragmentKind kind() {
        return ASTFragmentKind.SIMPLE_EXPRESSION;
    }

    public IASTFragment findMatchingSubFragment(IASTFragment other) {
        // cannot match method call fragments since there are no arguments here
        if (other.kind() == ASTFragmentKind.SIMPLE_EXPRESSION
                && new IsSameExpression().isSame(this.getAssociatedExpression(), other.getAssociatedExpression())) {
            return new SimpleExpressionASTFragment(expression);
        } else {
            return new EmptyASTFragment();
        }
    }
}
