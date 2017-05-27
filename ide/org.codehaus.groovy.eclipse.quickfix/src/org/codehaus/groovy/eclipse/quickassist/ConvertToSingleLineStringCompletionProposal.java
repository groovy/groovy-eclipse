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
 * Converts a multi-line string to a single-line string.
 */
public class ConvertToSingleLineStringCompletionProposal extends AbstractGroovyTextCompletionProposal {

    private Expression literal;

    public ConvertToSingleLineStringCompletionProposal(IInvocationContext context) {
        super(context);
    }

    public String getDisplayString() {
        return "Convert to single-line string";
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
            if (isMultiLineString(String.valueOf(nodeText))) {
                literal = (Expression) node;
                return true;
            }
        }

        return false;
    }

    @Override
    protected TextEdit getTextEdit(IDocument doc) throws BadLocationException {
        int startQuote = literal.getStart();
        int endQuote = literal.getEnd() - 3;
        if (startQuote < 0 || startQuote + 3 >= doc.getLength() || endQuote < 0 || endQuote + 3 > doc.getLength()) {
            return null;
        }
        String startText = doc.get(startQuote, 3);
        String endText = doc.get(endQuote, 3);
        if (!(startText.equals("\"\"\"") || startText.equals("'''"))) {
            return null;
        }
        if (!(endText.equals("\"\"\"") || endText.equals("'''"))) {
            return null;
        }
        String replaceQuote = String.valueOf(startText.charAt(0));
        TextEdit edit = new MultiTextEdit();
        edit.addChild(new ReplaceEdit(startQuote, 3, replaceQuote));
        edit.addChild(new ReplaceEdit(endQuote, 3, replaceQuote));

        boolean isSingle = replaceQuote.startsWith("'");

        // iterate through rest of list to unescape characters
        for (int i = startQuote + 3; i < endQuote - 3; i += 1) {
            char toEscape = doc.getChar(i);
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

        return edit;
    }

    // TODO: What about dollar slashy strings?
    private boolean isMultiLineString(String nodeText) {
        return (nodeText.startsWith("'''") && nodeText.endsWith("'''")) ||
            (nodeText.startsWith("\"\"\"") && nodeText.endsWith("\"\"\""));
    }
}
