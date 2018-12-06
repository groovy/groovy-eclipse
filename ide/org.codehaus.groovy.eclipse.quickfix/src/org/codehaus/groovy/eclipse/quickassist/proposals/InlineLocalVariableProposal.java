/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickassist.proposals;

import java.util.function.Consumer;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistProposal2;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

public class InlineLocalVariableProposal extends GroovyQuickAssistProposal2 {

    private VariableScope variableScope;
    private DeclarationExpression variableDeclaration;

    @Override
    public String getDisplayString() {
        return "Inline local variable";
    }

    @Override
    public Image getImage() {
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    }

    @Override
    public int getRelevance() {
        ASTNode coveredNode = context.getCoveredNode();
        if (coveredNode instanceof VariableExpression && ((VariableExpression) coveredNode).getAccessedVariable() instanceof VariableExpression) {
            TypeLookupResult result = context.getNodeType((VariableExpression) ((VariableExpression) coveredNode).getAccessedVariable());
            if (result != null && result.scope != null && result.enclosingAssignment instanceof DeclarationExpression &&
                    !((DeclarationExpression) result.enclosingAssignment).isMultipleAssignmentDeclaration()) {
                variableDeclaration = (DeclarationExpression) result.enclosingAssignment;
                variableScope = result.scope;

                // TODO: Does core Groovy offer an isEffectivelyFinal(VariableExpression)?

                try {
                    boolean[] found = new boolean[1];
                    forEachOccurrence(x -> found[0] = true);

                    if (found[0]) {
                        return 10;
                    }
                } catch (IllegalStateException ignore) {
                }
            }
        }
        return 0;
    }

    private String getValueExpression() {
        String value = context.getNodeText(variableDeclaration.getRightExpression());
        if (variableDeclaration.getRightExpression() instanceof BinaryExpression) {
            BinaryExpression valExp = (BinaryExpression) variableDeclaration.getRightExpression();
            if (valExp.getStart() == startOffset(valExp.getLeftExpression()) ||
                    valExp.getEnd() == endOffset(valExp.getRightExpression())) {
                value = "(" + value + ")";
            }
        }
        return value;
    }

    @Override
    protected TextChange getTextChange(IProgressMonitor monitor)
            throws BadLocationException {
        monitor.beginTask(getDisplayString(), 2);

        int limit = context.newTempDocument().getLineInformation(variableDeclaration.getLastLineNumber()).getOffset();
        int offset = variableDeclaration.getStart(), length = variableDeclaration.getLength();
        char[] source = context.getCompilationUnit().getContents();
        // consume whitespace after variable declaration up to end of line
        while ((offset + length) < limit && (offset + length) < source.length &&
                (Character.isWhitespace(source[offset + length]) || source[offset + length] == ';')) {
            length += 1;
        }
        monitor.worked(1);

        // delete variable declrartion and replace name references with its value
        MultiTextEdit edits = new MultiTextEdit();
        edits.addChild(new DeleteEdit(offset, length));
        String value = getValueExpression();
        forEachOccurrence(var -> {
            edits.addChild(new ReplaceEdit(var.getStart(), var.getLength(), value));
        });
        monitor.worked(1);

        try {
            return toTextChange(edits);
        } finally {
            monitor.done();
        }
    }

    private void forEachOccurrence(Consumer<VariableExpression> consumer) {
        VariableExpression variableExpr = variableDeclaration.getVariableExpression();
        VariableScope.VariableInfo variableInfo = variableScope.lookupNameInCurrentScope(variableExpr.getName());

        variableInfo.scopeNode.visit(new CodeVisitorSupport() {
            @Override
            public void visitVariableExpression(VariableExpression expression) {
                if (expression.getEnd() > 0 && expression != variableExpr &&
                        expression.getAccessedVariable() == variableExpr) {
                    consumer.accept(expression);
                }
                super.visitVariableExpression(expression);
            }

            @Override
            public void visitPostfixExpression(PostfixExpression expression) {
                if (expression.getExpression() instanceof VariableExpression &&
                        ((VariableExpression) expression.getExpression()).getAccessedVariable() == variableExpr) {
                    throw new IllegalStateException();
                }
                super.visitPostfixExpression(expression);
            }

            @Override
            public void visitPrefixExpression(PrefixExpression expression) {
                if (expression.getExpression() instanceof VariableExpression &&
                        ((VariableExpression) expression.getExpression()).getAccessedVariable() == variableExpr) {
                    throw new IllegalStateException();
                }
                super.visitPrefixExpression(expression);
            }

            @Override
            public void visitClosureListExpression(ClosureListExpression expression) {
                if (expression.getExpression(0) == variableDeclaration) {
                    throw new IllegalStateException();
                }
                super.visitClosureListExpression(expression);
            }

            @Override
            public void visitBinaryExpression(BinaryExpression expression) {
                if (expression != variableDeclaration && expression.getLeftExpression() instanceof VariableExpression &&
                        ((VariableExpression) expression.getLeftExpression()).getAccessedVariable() == variableExpr &&
                        Types.ofType(expression.getOperation().getType(), Types.ASSIGNMENT_OPERATOR)) {
                    throw new IllegalStateException();
                }
                super.visitBinaryExpression(expression);
            }
        });
    }

    private static int startOffset(ASTNode node) {
        int start;
        Long offsets = node.getNodeMetaData("source.offsets");
        if (offsets != null) {
            start = (int) (offsets >> 32);
        } else {
            start = node.getStart();
        }
        return start;
    }

    private static int endOffset(ASTNode node) {
        int end;
        Long offsets = node.getNodeMetaData("source.offsets");
        if (offsets != null) {
            end = (int) (offsets & 0xFFFFFFFF);
        } else {
            end = node.getEnd();
        }
        return end;
    }
}
