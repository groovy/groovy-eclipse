/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.codebrowsing.selection.IsSameExpression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

/**
 * A method call fragment.
 * <p>
 * This is the part of the method call expression that occurs after the '.'
 * and it includes the expression for the method selector as well as the
 * arguments expression.
 * <p>
 * The next {@link IASTFragment} is optional.
 */
public class MethodCallFragment implements IASTFragment {

    protected MethodCallExpression callExpression;
    private final Expression methodExpression;
    private final Expression arguments;
    private final IASTFragment next;
    private final int actualEndPosition;

    MethodCallFragment(Expression methodExpression, Expression arguments, IASTFragment next, int actualEndPosition) {
        this.methodExpression = methodExpression;
        this.arguments = arguments;
        this.next = next;
        this.actualEndPosition = actualEndPosition;
    }

    public MethodCallFragment(Expression methodExpression, Expression arguments, int actualEndPosition) {
        this(methodExpression, arguments, null, actualEndPosition);
    }

    @Override
    public ASTFragmentKind kind() {
        return ASTFragmentKind.METHOD_CALL;
    }

    public boolean hasNext() {
        return next != null;
    }

    public IASTFragment getNext() {
        return next;
    }

    @Override
    public int getStart() {
        return getAssociatedExpression().getStart();
    }

    @Override
    public int getEnd() {
        return hasNext() ? getNext().getEnd() : actualEndPosition;
    }

    @Override
    public int getLength() {
        return getEnd() - getStart();
    }

    @Override
    public int getTrimmedEnd(GroovyCompilationUnit unit) {
        if (hasNext()) {
            return getNext().getTrimmedEnd(unit);
        } else {
            char[] contents = unit.getContents();
            int end = Math.min(actualEndPosition, contents.length - 1);
            while (end > getStart() && Character.isWhitespace(contents[end])) {
                end -= 1;
            }
            return end;
        }
    }

    @Override
    public int getTrimmedLength(GroovyCompilationUnit unit) {
        return getTrimmedEnd(unit) - getStart();
    }

    @Override
    public MethodCallExpression getAssociatedNode() {
        return callExpression;
    }

    @Override
    public Expression getAssociatedExpression() {
        return methodExpression;
    }

    public Expression getArguments() {
        return arguments;
    }

    @Override
    public int fragmentLength() {
        return hasNext() ? 1 + getNext().fragmentLength() : 1;
    }

    @Override
    public void accept(FragmentVisitor visitor) {
        if (visitor.previsit(this) && visitor.visit(this) && hasNext()) {
            getNext().accept(visitor);
        }
    }

    @Override
    public String print(int indentLvl) {
        return ASTFragmentFactory.spaces(indentLvl) + "(M) " + getAssociatedExpression() +
            '.' + getArguments() + (hasNext() ? '\n' + getNext().print(indentLvl + 1) : "");
    }

    @Override
    public IASTFragment findMatchingSubFragment(IASTFragment that) {
        if (that.kind() == this.kind() && that.fragmentLength() <= this.fragmentLength() && similar((MethodCallFragment) that)) {
            IASTFragment frag = ((MethodCallFragment) that).next;
            if (frag == null) {
                // other has no more components, we have matched the current component only
                return new MethodCallFragment(this.getAssociatedExpression(), this.getArguments(), actualEndPosition);
            } else {
                frag = next.findMatchingSubFragment(frag);
                if (frag.kind() != ASTFragmentKind.EMPTY) {
                    return new MethodCallFragment(this.getAssociatedExpression(), this.getArguments(), frag, actualEndPosition);
                }
            }
        }
        return new EmptyASTFragment();
    }

    @Override
    public boolean matches(IASTFragment that) {
        if (that instanceof MethodCallFragment && ((MethodCallFragment) that).hasNext() == this.hasNext()) {
            return similar((MethodCallFragment) that);
        }
        return false;
    }

    protected boolean similar(MethodCallFragment that) {
        return new IsSameExpression().isSame(this.getAssociatedExpression(), that.getAssociatedExpression()) && new IsSameExpression().isSame(this.getArguments(), that.getArguments());
    }
}
