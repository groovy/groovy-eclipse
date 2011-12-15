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
import org.eclipse.core.runtime.Assert;

/**
 * This {@link IASTFragment} is any old ASTNode that is encapsulated as a
 * fragment
 *
 * @author andrew
 * @created Jun 7, 2010
 */
public class EnclosingASTNodeFragment implements IASTFragment {

    private final ASTNode node;

    EnclosingASTNodeFragment(ASTNode node) {
        this.node = node;
    }

    public void accept(FragmentVisitor visitor) {
        visitor.previsit(this);
        visitor.visit(this);
    }

    public IASTFragment findMatchingSubFragment(IASTFragment other) {
        return null;
    }

    public int fragmentLength() {
        return 1;
    }

    public Expression getAssociatedExpression() {
        Assert.isLegal(false, "EnclosingASTNodeFragment has no associated expression");
        return null;
    }

    public ASTNode getAssociatedNode() {
        return node;
    }

    public int getEnd() {
        return node.getEnd();
    }

    public int getStart() {
        return node.getStart();
    }

    public int getLength() {
        return getEnd() - getStart();
    }

    public int getTrimmedEnd(GroovyCompilationUnit unit) {
        char[] contents = unit.getContents();
        int end = getEnd();
        int start = node.getStart();
        while (end > start && Character.isWhitespace(contents[end])) {
            end--;
        }
        return end;
    }

    public int getTrimmedLength(GroovyCompilationUnit unit) {
        return getTrimmedEnd(unit) - getStart();
    }

    public ASTFragmentKind kind() {
        return ASTFragmentKind.ENCLOSING;
    }

    /**
     * Tests for == on Other's associated node.
     * this is because we do not yet have IsSame working on
     * anything except for expressions
     */
    public boolean matches(IASTFragment other) {
        return this.kind() == other.kind() && this.node == ((EnclosingASTNodeFragment) other).getAssociatedNode();
    }

    public String print(int indentLvl) {
        return "(E) " + node.toString();
    }

    @Override
    public String toString() {
        return print(0);
    }
}
