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
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Assert;

/**
 * This {@link IASTFragment} is any old ASTNode that is encapsulated as a
 * fragment.
 */
public class EnclosingASTNodeFragment implements IASTFragment {

    private final ASTNode node;

    EnclosingASTNodeFragment(ASTNode node) {
        this.node = node;
    }

    @Override
    public void accept(FragmentVisitor visitor) {
        visitor.previsit(this);
        visitor.visit(this);
    }

    @Override
    public IASTFragment findMatchingSubFragment(IASTFragment other) {
        return null;
    }

    @Override
    public int fragmentLength() {
        return 1;
    }

    @Override
    public Expression getAssociatedExpression() {
        Assert.isLegal(false, "EnclosingASTNodeFragment has no associated expression");
        return null;
    }

    @Override
    public ASTNode getAssociatedNode() {
        return node;
    }

    @Override
    public int getEnd() {
        return node.getEnd();
    }

    @Override
    public int getStart() {
        return node.getStart();
    }

    @Override
    public int getLength() {
        return getEnd() - getStart();
    }

    @Override
    public int getTrimmedEnd(GroovyCompilationUnit unit) {
        char[] contents = unit.getContents();
        int end = getEnd();
        int start = node.getStart();
        while (end > start && Character.isWhitespace(contents[end])) {
            end--;
        }
        return end;
    }

    @Override
    public int getTrimmedLength(GroovyCompilationUnit unit) {
        return getTrimmedEnd(unit) - getStart();
    }

    @Override
    public ASTFragmentKind kind() {
        return ASTFragmentKind.ENCLOSING;
    }

    /**
     * Tests for == on Other's associated node.
     * this is because we do not yet have IsSame working on
     * anything except for expressions
     */
    @Override
    public boolean matches(IASTFragment other) {
        return this.kind() == other.kind() && this.node == ((EnclosingASTNodeFragment) other).getAssociatedNode();
    }

    @Override
    public String print(int indentLvl) {
        return "(E) " + node.toString();
    }

    @Override
    public String toString() {
        return print(0);
    }
}
