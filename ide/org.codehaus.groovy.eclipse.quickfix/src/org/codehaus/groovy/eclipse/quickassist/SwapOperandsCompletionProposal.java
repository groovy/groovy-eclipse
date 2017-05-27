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
package org.codehaus.groovy.eclipse.quickassist;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Exchanges the left and right operands of an infix expression.
 * <p>
 * Ex: "(a && b)" becomes "(b && a)"
 */
public class SwapOperandsCompletionProposal extends AbstractGroovyTextCompletionProposal {

    private BinaryExpression binaryExpression;

    public SwapOperandsCompletionProposal(IInvocationContext context) {
        super(context);
    }

    public String getDisplayString() {
        return "Exchange left and right operands for infix expression";
    }

    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_CORRECTION_CHANGE;
    }

    public boolean hasProposals() {
        Region region = new Region(context.getSelectionOffset(), context.getSelectionLength());
        ASTNode node = new ASTNodeFinder(region).doVisit(getGroovyCompilationUnit().getModuleNode());
        if (node instanceof BinaryExpression) {
            BinaryExpression expr = (BinaryExpression) node;
            if (isApplicableOperator(expr.getOperation().getType())) {
                binaryExpression = expr;
                return true;
            }
        }
        return false;
    }

    @Override
    protected TextEdit getTextEdit(IDocument document) throws BadLocationException {
        Expression left = binaryExpression.getLeftExpression();
        Expression right = binaryExpression.getRightExpression();
        String leftText = document.get(left.getStart(), left.getLength()).trim();
        String rightText = document.get(right.getStart(), right.getLength()).trim();

        TextEdit edit = new MultiTextEdit();
        edit.addChild(new ReplaceEdit(right.getStart(), rightText.length(), leftText));
        edit.addChild(new ReplaceEdit(left.getStart(), leftText.length(), rightText));
        return edit;
    }

    private static boolean isApplicableOperator(int op) {
        switch (op) {
        case Types.PLUS:
        case Types.MINUS:
        case Types.MULTIPLY:
        case Types.DIVIDE:
        case Types.MOD:
        case Types.POWER:
        case Types.LOGICAL_AND:
        case Types.LOGICAL_OR:
        case Types.BITWISE_AND:
        case Types.BITWISE_OR:
        case Types.BITWISE_XOR:
        case Types.COMPARE_TO:
        case Types.COMPARE_EQUAL:
        case Types.COMPARE_NOT_EQUAL:
        case Types.COMPARE_LESS_THAN:
        case Types.COMPARE_LESS_THAN_EQUAL:
        case Types.COMPARE_GREATER_THAN:
        case Types.COMPARE_GREATER_THAN_EQUAL:
        case Types.LEFT_SHIFT:
        case Types.RIGHT_SHIFT:
        case Types.RIGHT_SHIFT_UNSIGNED:
            return true;
        default:
            return false;
        }
    }
}
