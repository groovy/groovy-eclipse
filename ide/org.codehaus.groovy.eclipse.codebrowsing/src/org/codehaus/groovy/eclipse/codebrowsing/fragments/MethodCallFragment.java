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
 * A method call fragment
 * This is the part of the method call expression that occurs after the '.'
 * and it includes the expression for the method selector as well as the
 * arguments expression.
 *
 * The next {@link IASTFragment} is optional.
 *
 * @author andrew
 * @created Jun 4, 2010
 */
public class MethodCallFragment implements IASTFragment {

    private final Expression arguments;

    private final Expression methodExpression;

    private final IASTFragment next;

    private int actualEndPosition;

    MethodCallFragment(Expression methodExpression, Expression arguments, IASTFragment next, int actualEndPosition) {
        this.methodExpression = methodExpression;
        this.arguments = arguments;
        this.next = next;
        this.actualEndPosition = actualEndPosition;
    }

    public MethodCallFragment(Expression methodExpression, Expression arguments, int actualEndPosition) {
        this(methodExpression, arguments, null, actualEndPosition);
    }

    public boolean hasNext() {
        return next != null;
    }

    public int getEnd() {
        return hasNext() ? next.getEnd() : actualEndPosition;
    }

    public int getStart() {
        return methodExpression.getStart();
    }

    public int getLength() {
        return getEnd() - getStart();
    }

    public int getTrimmedEnd(GroovyCompilationUnit unit) {
        if (hasNext()) {
            return getNext().getTrimmedEnd(unit);
        } else {
            char[] contents = unit.getContents();
            int end = actualEndPosition;
            while (end > methodExpression.getStart() && Character.isWhitespace(contents[end])) {
                end--;
            }
            return end;
        }
    }

    public int getTrimmedLength(GroovyCompilationUnit unit) {
        return getTrimmedEnd(unit) - getStart();
    }

    public Expression getArguments() {
        return arguments;
    }

    public IASTFragment getNext() {
        return next;
    }

    public boolean matches(IASTFragment other) {
        if (!(other instanceof MethodCallFragment)) {
            return false;
        }

        MethodCallFragment otherCall = (MethodCallFragment) other;
        if (otherCall.hasNext() != hasNext()) {
            return false;
        }

        return new IsSameExpression().isSame(arguments, otherCall.getArguments())
                && new IsSameExpression().isSame(methodExpression, otherCall.getAssociatedExpression());
    }

    public int fragmentLength() {
        return hasNext() ? 1 + next.fragmentLength() : 1;
    }

    /**
     * hmmmm....is this right. There are actually two associated
     * nodes, the methodExpression and the arguments.
     */
    public Expression getAssociatedExpression() {
        return methodExpression;
    }

    public ASTNode getAssociatedNode() {
        return methodExpression;
    }

    public String print(int indentLvl) {
        return ASTFragmentFactory.spaces(indentLvl) + "(M) " + methodExpression + "." + arguments
                + (hasNext() ? "\n" + next.print(indentLvl + 1) : "");
    }

    public void accept(FragmentVisitor visitor) {
        if (visitor.previsit(this) && visitor.visit(this) && hasNext()) {
            next.accept(visitor);
        }
    }

    public ASTFragmentKind kind() {
        return ASTFragmentKind.METHOD_CALL;
    }

    public IASTFragment findMatchingSubFragment(IASTFragment other) {
        if (other.fragmentLength() > this.fragmentLength()) {
            return new EmptyASTFragment();
        }

        // can only match other method call fragments
        if (other.kind() == this.kind()) {
            MethodCallFragment toMatchCall = (MethodCallFragment) other;
            if (new IsSameExpression().isSame(this.getAssociatedExpression(), toMatchCall.getAssociatedExpression())
                    && new IsSameExpression().isSame(this.getArguments(), toMatchCall.getArguments())) {
                if (!toMatchCall.hasNext()) {
                    // other has no more components, we have matched the current
                    // component only
                    return new MethodCallFragment(this.getAssociatedExpression(), this.getArguments(), this.actualEndPosition);
                } else {
                    IASTFragment result = this.getNext().findMatchingSubFragment(toMatchCall.getNext());
                    if (result.kind() == ASTFragmentKind.EMPTY) {
                        // the target expressions match, but not the remainder
                        // no match is possible
                        return new EmptyASTFragment();
                    } else {
                        // remainder (or part of it) matches.
                        MethodCallFragment newFragment = new MethodCallFragment(this.getAssociatedExpression(),
                                this.getArguments(), result, this.actualEndPosition);
                        return newFragment;
                    }
                }
            }
        }
        return new EmptyASTFragment();
    }
}
