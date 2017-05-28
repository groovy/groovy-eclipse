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
 * Converts a single-line string to a multi-line string.
 */
public class ConvertToMultiLineStringProposal extends GroovyQuickAssistProposal2 {

    @Override
    public String getDisplayString() {
        return "Convert to multi-line string";
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
                if (isStringLiteral(nodeText) && !isMultiLineString(nodeText)) {
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
        int endQuote = string.getEnd() - 1;
        if (startQuote < 0 || startQuote >= contents.length || endQuote <= startQuote || endQuote >= contents.length) {
            return null;
        }
        if (!(contents[startQuote] == '\'' || contents[startQuote] == '"')) {
            return null;
        }
        if (!(contents[endQuote] == '\'' || contents[endQuote] == '"')) {
            return null;
        }
        char quoteChar = contents[startQuote];
        char skipChar = '\0';
        String replaceQuotes = new String(new char[] {quoteChar, quoteChar, quoteChar});
        TextEdit edit = new MultiTextEdit();
        edit.addChild(new ReplaceEdit(startQuote, 1, replaceQuotes));
        edit.addChild(new ReplaceEdit(endQuote, 1, replaceQuotes));

        // iterate through rest of list to unescape characters
        for (int i = startQuote + 1; i < endQuote - 1; i += 1) {
            if (contents[i] == '\\') {
                i++;
                if (contents[i] != skipChar) {
                    edit.addChild(new ReplaceEdit(i - 1, 2, unescaped(contents[i])));
                }
            }
        }

        return toTextChange(edit);
    }

    // TODO: What about slashy strings?
    private boolean isStringLiteral(String nodeText) {
        int length = nodeText.length();
        if (length > 1) {
            char first = nodeText.charAt(0), last = nodeText.charAt(length - 1);
            return ((first == '\'' || first == '"') && first == last);
        }
        return false;
    }

    // TODO: What about dollar slashy strings?
    private boolean isMultiLineString(String nodeText) {
        return (nodeText.startsWith("'''") && nodeText.endsWith("'''")) ||
            (nodeText.startsWith("\"\"\"") && nodeText.endsWith("\"\"\""));
    }

    private String unescaped(char escaped) {
        switch (escaped) {
        case '\'':
            return "'";
        case '"':
            return "\"";
        case '\\':
            return "\\";
        case 'n':
            return "\n";
        case 'r':
            return "\r";
        case 't':
            return "\t";
        case 'f':
            // the \f character seems to cause errors in the editor
            return "\n";
        case 'b':
            return "\b";
        case 'u':
            // don't try to convert unicode characters
        }
        // shouldn't get here
        return String.valueOf(escaped);
    }
}
