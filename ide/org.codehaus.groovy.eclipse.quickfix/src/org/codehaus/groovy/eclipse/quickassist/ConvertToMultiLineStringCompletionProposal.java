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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Converts a single-line string to a multi-line string.
 */
public class ConvertToMultiLineStringCompletionProposal extends AbstractGroovyTextCompletionProposal {

    private Expression literal;

    public ConvertToMultiLineStringCompletionProposal(IInvocationContext context) {
        super(context);
    }

    public String getDisplayString() {
        return "Convert to multi-line string";
    }

    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_CORRECTION_CHANGE;
    }

    public boolean hasProposals() {
        Region region = new Region(context.getSelectionOffset(), context.getSelectionLength());
        GroovyCompilationUnit unit = getGroovyCompilationUnit();
        ASTNodeFinder finder = new StringConstantFinder(region);
        ASTNode node = finder.doVisit(unit.getModuleNode());

        if (node != null && node.getEnd() > node.getStart() && (node instanceof GStringExpression ||
                (node instanceof ConstantExpression && ((ConstantExpression) node).getValue() instanceof String))) {
            String nodeText = String.valueOf(unit.getContents(), node.getStart(), node.getLength());
            if (isStringLiteral(nodeText) && !isMultiLineString(String.valueOf(nodeText))) {
                literal = (Expression) node;
                return true;
            }
        }

        return false;
    }

    @Override
    protected TextEdit getTextEdit(IDocument doc) throws BadLocationException {
        int startQuote = literal.getStart();
        int endQuote = literal.getEnd() - 1;
        if (startQuote < 0 || startQuote >= doc.getLength() || endQuote <= startQuote || endQuote >= doc.getLength()) {
            return null;
        }
        if (!(doc.getChar(startQuote) == '\'' || doc.getChar(startQuote) == '"')) {
            return null;
        }
        if (!(doc.getChar(endQuote) == '\'' || doc.getChar(endQuote) == '"')) {
            return null;
        }
        char quoteChar = doc.getChar(startQuote);
        char skipChar = '\0';
        String replaceQuotes = new String(new char[] { quoteChar, quoteChar, quoteChar });
        TextEdit edit = new MultiTextEdit();
        edit.addChild(new ReplaceEdit(startQuote, 1, replaceQuotes));
        edit.addChild(new ReplaceEdit(endQuote, 1, replaceQuotes));

        // iterate through rest of list to unescape characters
        for (int i = startQuote + 1; i < endQuote - 1; i += 1) {
            if (doc.getChar(i) == '\\') {
                i++;
                if (doc.getChar(i) != skipChar) {
                    edit.addChild(new ReplaceEdit(i - 1, 2, unescaped(doc.getChar(i))));
                }
            }
        }

        return edit;
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
        case 'u':
            // don't try to convert unicode characters
            return "u";
        case 't':
            return "\t";
        case 'b':
            return "\b";
        case 'n':
            return "\n";
        case 'r':
            return "\r";
        case 'f':
            // the \f character seems to cause errors in the editor
            return "\n";
        case '\'':
            return "'";
        case '"':
            return "\"";
        case '\\':
            return "\\";
        }
        // shouldn't get here
        return String.valueOf(escaped);
    }
}
