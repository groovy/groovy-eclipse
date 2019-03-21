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
package org.codehaus.groovy.eclipse.quickassist.proposals;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistProposal2;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Converts a multi-line string to a single-line string.
 */
public class ConvertToSingleLineStringProposal extends GroovyQuickAssistProposal2 {

    @Override
    public String getDisplayString() {
        return "Convert to single-line string";
    }

    @Override
    public Image getImage() {
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    }

    private Expression string;

    @Override
    public int getRelevance() {
        if (string == null) {
            ASTNode node = context.getCoveredNode();
            if ((node instanceof GStringExpression || (node instanceof ConstantExpression &&
                    ((ConstantExpression) node).getValue() instanceof String)) && node.getEnd() > node.getStart()) {
                String nodeText = context.getNodeText(node);
                if (isMultiLineString(nodeText)) {
                    string = (Expression) node;
                }
            }
        }
        return (string != null ? 10 : 0);
    }

    @Override
    protected TextChange getTextChange(IProgressMonitor monitor) {
        char[] contents = context.getCompilationUnit().getContents();
        int startQuote = string.getStart();
        int endQuote = string.getEnd() - 3;
        if (startQuote < 0 || startQuote + 3 >= contents.length || endQuote < 0 || endQuote + 3 > contents.length) {
            return null;
        }
        boolean isSingle = contents[startQuote] == '\'';
        String replaceQuote = String.valueOf(contents[startQuote]);

        TextEdit edit = new MultiTextEdit();
        edit.addChild(new ReplaceEdit(startQuote, 3, replaceQuote));
        edit.addChild(new ReplaceEdit(endQuote, 3, replaceQuote));

        // iterate through rest of list to unescape characters
        for (int i = startQuote + 3; i < endQuote - 3; i += 1) {
            char toEscape = contents[i];
            String escaped = null;
            switch (toEscape) {
            case '\t':
                escaped = "\\t";
                break;
            case '\b':
                escaped = "\\b";
                break;
            case '\n':
                escaped = "\\n";
                break;
            case '\r':
                escaped = "\\r";
                break;
            case '\f':
                escaped = "\\f";
                break;
            case '\'':
                if (isSingle)
                    escaped = "\\'";
                break;
            case '"':
                if (!isSingle)
                    escaped = "\\\"";
                break;
            case '\\':
                escaped = "\\\\";
                break;
            }
            if (escaped != null) {
                edit.addChild(new ReplaceEdit(i, 1, escaped));
            }
        }

        return toTextChange(edit);
    }

    // TODO: What about dollar slashy strings?
    private boolean isMultiLineString(String nodeText) {
        return (nodeText.startsWith("'''") && nodeText.endsWith("'''")) ||
            (nodeText.startsWith("\"\"\"") && nodeText.endsWith("\"\"\""));
    }
}
