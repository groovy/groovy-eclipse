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
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

/**
 * FIXADE convert to singleton
 *
 * @author andrew
 * @created Jun 6, 2010
 */
public class EmptyASTFragment implements IASTFragment {

    // all empty expressions are really the same
    private static final EmptyExpression emptyExpression = new EmptyExpression();

    public void accept(FragmentVisitor visitor) {
        visitor.previsit(this);
        visitor.visit(this);
    }

    public int fragmentLength() {
        return 0;
    }

    public Expression getAssociatedExpression() {
        return emptyExpression;
    }

    public ASTNode getAssociatedNode() {
        return emptyExpression;
    }

    public int getEnd() {
        return 0;
    }

    public int getStart() {
        return 0;
    }

    public int getLength() {
        return 0;
    }

    public int getTrimmedEnd(GroovyCompilationUnit unit) {
        return 0;
    }

    public int getTrimmedLength(GroovyCompilationUnit unit) {
        return getTrimmedEnd(unit) - getStart();
    }

    public ASTFragmentKind kind() {
        return ASTFragmentKind.EMPTY;
    }

    public boolean matches(IASTFragment other) {
        return other.kind() == ASTFragmentKind.EMPTY;
    }

    public String print(int indentLvl) {
        return ASTFragmentFactory.spaces(indentLvl) + "(E) empty";
    }

    public IASTFragment findMatchingSubFragment(IASTFragment other) {
        return this;
    }

    @Override
    public String toString() {
        return print(0);
    }

}
