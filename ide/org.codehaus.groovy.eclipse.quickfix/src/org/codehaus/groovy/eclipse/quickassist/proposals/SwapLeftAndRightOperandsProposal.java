/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickassist.proposals;

import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistProposal2;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Exchanges the left and right operands of an infix expression.
 * <p>
 * Ex: "(a && b)" becomes "(b && a)"
 */
public class SwapLeftAndRightOperandsProposal extends GroovyQuickAssistProposal2 {

    @Override
    public String getDisplayString() {
        return "Exchange left and right operands for infix expression";
    }

    @Override
    public Image getImage() {
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    }

    @Override
    public int getRelevance() {
        if (context.getCoveredNode() instanceof BinaryExpression) {
            switch (((BinaryExpression) context.getCoveredNode()).getOperation().getType()) {
            case Types.PLUS:
            case Types.MINUS:
            case Types.MULTIPLY:
            case Types.LOGICAL_AND:
            case Types.LOGICAL_OR:
            case Types.BITWISE_AND:
            case Types.BITWISE_OR:
            case Types.BITWISE_XOR:
            case Types.COMPARE_TO:
            case Types.COMPARE_EQUAL:
            case Types.COMPARE_NOT_EQUAL:
            case Types.COMPARE_IDENTICAL:
            case Types.COMPARE_NOT_IDENTICAL:
            case Types.COMPARE_LESS_THAN:
            case Types.COMPARE_LESS_THAN_EQUAL:
            case Types.COMPARE_GREATER_THAN:
            case Types.COMPARE_GREATER_THAN_EQUAL:
                return 10;
            }
        }
        return 0;
    }

    @Override
    protected TextChange getTextChange(IProgressMonitor monitor) {
        Expression lhs = ((BinaryExpression) context.getCoveredNode()).getLeftExpression();
        Expression rhs = ((BinaryExpression) context.getCoveredNode()).getRightExpression();
        String lhsText = context.getNodeText(lhs).trim();
        String rhsText = context.getNodeText(rhs).trim();

        TextEdit edit = new MultiTextEdit();
        edit.addChild(new ReplaceEdit(rhs.getStart(), rhsText.length(), lhsText));
        edit.addChild(new ReplaceEdit(lhs.getStart(), lhsText.length(), rhsText));
        return toTextChange(edit);
    }
}
