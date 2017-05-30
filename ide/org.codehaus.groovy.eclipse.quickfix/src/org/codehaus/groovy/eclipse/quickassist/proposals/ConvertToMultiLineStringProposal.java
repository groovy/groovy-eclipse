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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistProposal2;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.InsertEdit;
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

    private List<Expression> expressions;

    @Override
    public int getRelevance() {
        if (expressions == null) {
            expressions = Collections.emptyList();
            ASTNode node = context.getCoveredNode();
            if (isLiteralString(node) && !isMultilineString(context.getNodeText(node))) {
                expressions = Collections.singletonList((Expression) node);
            } else if (node instanceof BinaryExpression &&
                    ((BinaryExpression) node).getOperation().getType() == Types.PLUS) {
                // check for string concatenation sequence
                TypeLookupResult result = context.getNodeType(node);
                if (VariableScope.STRING_CLASS_NODE.equals(result.type)) {
                    BinaryExpression expr = (BinaryExpression) node;
                    LinkedList<Expression> stack = new LinkedList<Expression>();
                    for (;;) { // accumulate left expressions
                        stack.addFirst(expr.getRightExpression());
                        if (expr.getLeftExpression() instanceof BinaryExpression) {
                            expr = (BinaryExpression) expr.getLeftExpression();
                            switch (expr.getOperation().getType()) {
                            case Types.PLUS:
                                continue;
                            default:
                                return 0;
                            }
                        }
                        break;
                    }
                    stack.addFirst(expr.getLeftExpression());

                    // at least one must be a string literal
                    for (Expression e : stack) {
                        if (isLiteralString(e)) {
                            expressions = stack;
                            break;
                        }
                    }
                }
            }
        }
        return (!expressions.isEmpty() ? 10 : 0);
    }

    @Override
    protected TextChange getTextChange(IProgressMonitor monitor) {
        monitor.beginTask(getDisplayString(), 1 + expressions.size());

        // is GString required?
        boolean interpolated = false;
        for (Expression expr : expressions) {
            if (isGString(expr) || !isLiteralString(expr)) {
                interpolated = true;
                break;
            }
        }
        char quote = interpolated ? '"' : '\'';
        char[] source = context.getCompilationUnit().getContents();

        // insert opening quotations
        TextEdit edit = new MultiTextEdit();
        edit.addChild(new InsertEdit(expressions.get(0).getStart(), String.valueOf(new char[] {quote, quote, quote})));

        monitor.worked(1);

        Expression last = null;
        for (Expression expr : expressions) {
            boolean isLiteralString = isLiteralString(expr),
                    hasTripleQuotes = isLiteralString && isMultilineString(context.getNodeText(expr));

            // determine leading replacement
            int length = 0, offset = expr.getStart();
            String replacement = isLiteralString ? "" : "${";
            if (last != null) {
                // remove whitespace, quote(s) and '+'
                while (offset > 0 && source[offset - 1] == '+' || Character.isWhitespace(source[offset - 1])) {
                    length += 1;
                    offset -= 1;
                }
                // if last was on another line and didn't end with newline, include a backslash and newline
                if (expr.getLineNumber() != last.getLastLineNumber() &&
                        (!isLiteralString(last) || !isNewlineTerminated(context.getNodeText(last)))) {
                    replacement = '\\' + unescaped('n') + replacement;
                }
            }
            if (isLiteralString) length += hasTripleQuotes ? 3 : 1;
            else if (expr instanceof ClosureExpression) length += 1;
            edit.addChild(new ReplaceEdit(offset, length, replacement));

            if (isLiteralString) {
                int max = expr.getEnd() - (hasTripleQuotes ? 3 : 1),
                    end = max;
                for (ConstantExpression str : getStrings(expr)) {
                    // deal with some escaping
                    end = Math.min(str.getEnd(), max);
                    for (int pos = offset + length; pos < end; pos += 1) {
                        if (source[pos] == '\\' && (replacement = unescaped(source[pos + 1])) != null) {
                            edit.addChild(new ReplaceEdit(pos++, 2, replacement));
                        } else if (interpolated && source[pos] == '$' && !isGString(expr)) {
                            edit.addChild(new InsertEdit(pos++, "\\"));
                        } else if (pos + 2 < end && source[pos] == quote &&
                                source[pos + 1] == quote && source[pos + 2] == quote) {
                            // TODO: escape triple-quote
                        }
                        // TODO: escape trailing quote chars to prevent early termination; ex: "'" -> '''\''''
                    }
                }

                // remove trailing quote(s)
                edit.addChild(new ReplaceEdit(end, hasTripleQuotes ? 3 : 1, ""));
            } else if (!(expr instanceof ClosureExpression)) {
                edit.addChild(new InsertEdit(expr.getEnd(), "}"));
            }

            last = expr;
            monitor.worked(1);
        }

        // insert closing quotations
        edit.addChild(new InsertEdit(expressions.get(expressions.size() - 1).getEnd(), String.valueOf(new char[] {quote, quote, quote})));

        try {
            return toTextChange(edit);
        } finally {
            monitor.done();
        }
    }

    //--------------------------------------------------------------------------

    private List<ConstantExpression> getStrings(Expression expr) {
        if (isGString(expr)) {
            return ((GStringExpression) expr).getStrings();
        }
        return Collections.singletonList((ConstantExpression) expr);
    }

    private boolean isGString(ASTNode node) {
        return node instanceof GStringExpression;
    }

    private boolean isLiteralString(ASTNode node) {
        if ((isGString(node) || (node instanceof ConstantExpression &&
                ((ConstantExpression) node).getValue() instanceof String)) && node.getEnd() > node.getStart()) {
            return isLiteralString(context.getNodeText(node));
        }
        return false;
    }

    private boolean isLiteralString(String nodeText) {
        boolean result = false;
        int length = nodeText.length();
        if (length > 1) {
            char first = nodeText.charAt(0), last = nodeText.charAt(length - 1);
            result = ((first == '\'' || first == '"') && first == last);
        }
        return result;
    }

    private boolean isMultilineString(String nodeText) {
        boolean result = false;
        if (nodeText.startsWith("\"\"\"") && nodeText.endsWith("\"\"\"")) {
            result = true;
        } else if (nodeText.startsWith("'''") && nodeText.endsWith("'''")) {
            result = true;
        }
        return result;
    }

    private boolean isNewlineTerminated(String nodeText) {
        boolean result = false;
        int i = nodeText.length() - (isMultilineString(nodeText) ? 3 : 1) - 1;
        if (i > 1) {
            result = nodeText.charAt(i) == '\n' || (nodeText.charAt(i) == 'n' && nodeText.charAt(i - 1) == '\\');
        }
        return result;
    }

    private String unescaped(char escaped) {
        switch (escaped) {
        case '\'':
            return "'";
        case '"':
            return "\"";
        case 't':
            return "\t";
        case '\\':
            return "\\";
        case 'n':
            if (lineDelimiter == null) {
                lineDelimiter = context.getLineDelimiter(null, 1);
            }
            return lineDelimiter;
        }
        return null;
    }

    private String lineDelimiter;
}
