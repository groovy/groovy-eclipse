/*
 * Copyright 2009-2021 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import groovyjarjarantlr.Token;
import org.codehaus.groovy.antlr.GroovyTokenTypeBridge;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.refactoring.PreferenceConstants;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTScanner;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.ClosuresInCodePredicate;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

@SuppressWarnings("deprecation")
public class GroovyBeautifier {

    private static final boolean DEBUG_EDITS = false; // TODO: Read value using Platform.getDebugOption

    public final DefaultGroovyFormatter formatter;
    private final IFormatterPreferences preferences;
    private final Set<Token> ignoreToken = new HashSet<>();

    public GroovyBeautifier(DefaultGroovyFormatter formatter, IFormatterPreferences preferences) {
        this.formatter = formatter;
        this.preferences = preferences;
    }

    public TextEdit getBeautifiEdits() throws BadLocationException {
        try {
            MultiTextEdit edits = new MultiTextEdit();

            combineClosures(edits);
            formatLists(edits);
            correctBraces(edits);
            removeUnnecessarySemicolons(edits);

            return edits;
        } finally {
            formatter.getTokens().dispose();
        }
    }

    private void combineClosures(MultiTextEdit edits) throws BadLocationException {
        ASTScanner scanner = new ASTScanner(formatter.getProgressRootNode(), new ClosuresInCodePredicate(), formatter.getProgressDocument());
        scanner.startASTscan();
        for (ASTNode node : scanner.getMatchedNodes().keySet()) {
            ClosureExpression clExp = ((ClosureExpression) node);

            int posClStart = formatter.getPosOfToken(GroovyTokenTypeBridge.LCURLY, clExp.getLineNumber(), clExp.getColumnNumber(), "{");
            if (posClStart == -1) {
                // skip... invalid (likely the closure is inside a GString so can't find tokens in there.
                continue;
            }

            int posCLEnd = formatter.getPosOfToken(GroovyTokenTypeBridge.RCURLY, clExp.getLastLineNumber(), clExp.getLastColumnNumber() - 1, "}");
            if (posCLEnd == -1) {
                int positionLastTokenOfClosure = formatter.getPosOfToken(clExp.getLastLineNumber(), clExp.getLastColumnNumber());
                while (formatter.getTokens().get(positionLastTokenOfClosure).getType() != GroovyTokenTypeBridge.RCURLY) {
                    positionLastTokenOfClosure -= 1;
                }
                posCLEnd = positionLastTokenOfClosure;
            }
            // ignore closure on one line
            if (clExp.getLineNumber() == clExp.getLastLineNumber()) {
                ignoreToken.add(formatter.getTokens().get(posCLEnd));
                continue;
            }

            if (clExp.getCode() instanceof BlockStatement) {
                int posParamDelim = posClStart;
                if (clExp.isParameterSpecified()) {
                    // position Parameters on same line
                    posParamDelim = formatter.getPosOfNextTokenOfType(posClStart, GroovyTokenTypeBridge.CLOSABLE_BLOCK_OP);
                    replaceNLSWithSpace(edits, posClStart, posParamDelim);
                }
                // check if there is a linebreak after the parameters
                if (posParamDelim > 0 && formatter.getNextTokenIncludingNLS(posParamDelim).getType() != GroovyTokenTypeBridge.NLS) {
                    addEdit(new InsertEdit(formatter.getOffsetOfTokenEnd(formatter.getTokens().get(posParamDelim)), formatter.getNewLine()), edits);
                } else {
                    // if there are no parameters check if the first statement is on the next line
                    if (posParamDelim == 0 && formatter.getNextTokenIncludingNLS(posClStart).getType() != GroovyTokenTypeBridge.NLS) {
                        addEdit(new InsertEdit(formatter.getOffsetOfTokenEnd(formatter.getTokens().get(posClStart)), formatter.getNewLine()), edits);
                    }
                }
            }
        }
    }

    private void formatLists(MultiTextEdit edits) {
        ASTScanner scanner = new ASTScanner(formatter.getProgressRootNode(), new ListInCodePredicate(), formatter.getProgressDocument());
        scanner.startASTscan();
        for (ASTNode node : scanner.getMatchedNodes().keySet()) {
            ListExpression listExpr = (ListExpression) node;
            GroovyDocumentScanner tokens = formatter.getTokens();
            try {
                int lbrackOffset = listExpr.getStart();
                int rbrackOffset = listExpr.getEnd() - 1;
                while (Character.isWhitespace(formatter.getProgressDocument().getChar(rbrackOffset))) {
                    rbrackOffset -= 1;
                }
                assert formatter.getProgressDocument().getChar(lbrackOffset) == '[';
                assert formatter.getProgressDocument().getChar(rbrackOffset) == ']';

                Token penultimate = tokens.getLastNonWhitespaceTokenBefore(rbrackOffset);
                if (penultimate.getType() == GroovyTokenTypeBridge.STRING_CTOR_START) {
                    // this means we are inside a GString and we won't apply edits here
                    continue;
                }

                boolean wrap = (rbrackOffset - lbrackOffset > preferences.getLongListLength() ||
                    (listExpr.getExpressions().size() > 1 && hasClosureElement(listExpr)));

                for (Expression exp : listExpr.getExpressions()) {
                    Token previous = tokens.getLastTokenBefore(exp.getStart());
                    while (previous.getType() != GroovyTokenTypeBridge.LBRACK && previous.getType() != GroovyTokenTypeBridge.COMMA) {
                        previous = tokens.getLastTokenBefore(previous);
                    }
                    String prefix = (wrap ? formatter.getNewLine() : previous.getType() == GroovyTokenTypeBridge.LBRACK ? "" : " ");
                    replaceWhiteSpaceAfter(edits, previous, prefix);
                }
                String suffix = (wrap || penultimate.getType() == GroovyTokenTypeBridge.SL_COMMENT ? formatter.getNewLine() : "");
                replaceWhiteSpaceAfter(edits, penultimate, suffix);
            } catch (Exception e) {
                Util.log(e);
            }
        }
    }

    /**
     * Creates edit that replaces whitespace tokens immediately following given
     * token with specified string.
     */
    private void replaceWhiteSpaceAfter(MultiTextEdit edits, Token token, String replacement) {
        GroovyDocumentScanner tokens = formatter.getTokens();
        try {
            Token first = tokens.getNextToken(token); // first whitespace token (if any)
            Token last = first; // first non-whitespace token
            // if no white space tokens where found then first and last will be the same token (i.e. token just after the given token)
            while (isWhiteSpace(last.getType())) {
                last = tokens.getNextToken(last);
            }
            int offset = tokens.getEnd(token);
            int length = tokens.getOffset(last) - offset;
            addEdit(new ReplaceEdit(offset, length, replacement), edits);
        } catch (BadLocationException e) {
            Util.log(e);
        }
    }

    private boolean isWhiteSpace(int type) {
        return (type == GroovyTokenTypeBridge.WS || type == GroovyTokenTypeBridge.NLS);
    }

    private boolean hasClosureElement(ListExpression node) {
        List<Expression> list = node.getExpressions();
        for (int i = 0; i < list.size(); i += 1) {
            if (list.get(i) instanceof ClosureExpression) {
                return true;
            }
        }
        return false;
    }

    private void replaceNLSWithSpace(MultiTextEdit container, int startPos, int endPos) throws BadLocationException {
        Token fromToken = null; // remember first NLS token in a string of NLS tokens
        int p = startPos + 1;
        while (p < endPos) {
            Token token = formatter.getTokens().get(p);
            int ttype = token.getType();
            if (ttype == GroovyTokenTypeBridge.NLS) {
                if (fromToken == null) {
                    fromToken = token;
                }
            } else {
                if (ttype == GroovyTokenTypeBridge.SL_COMMENT) {
                    p += 1; // next token will be skipped whether it is a NLS or not!
                }
                if (fromToken != null) {
                    // replace NLS tokens from fromToken up to current token
                    addEdit(newReplaceEdit(fromToken, token, " "), container);
                    fromToken = null;
                }
            }
            p += 1;
        }
        // don't forget to replace nls tokens at the end.
        if (fromToken != null) {
            Token token = formatter.getTokens().get(p);
            addEdit(newReplaceEdit(fromToken, token, " "), container);
        }
    }

    /**
     * Creates an edit that replaces text from the start of fromToken to the start of toToken.
     *
     * @param fromToken Where to start replacing text.
     * @param toToken Where to end replacing text.
     * @param replacement The text to replace the original text with.
     */
    private TextEdit newReplaceEdit(Token fromToken, Token toToken, String replacement) throws BadLocationException {
        int offset = formatter.getOffsetOfToken(fromToken);
        int length = formatter.getOffsetOfToken(toToken) - offset;
        return new ReplaceEdit(offset, length, replacement);
    }

    private void correctBraces(MultiTextEdit edits) throws BadLocationException {
        BiFunction<Integer, Token, TextEdit> lCurlyEditor = getBracesEditor(preferences.getBracesStart());
        BiFunction<Integer, Token, TextEdit> rCurlyEditor = getBracesEditor(preferences.getBracesEnd());
        KlenkDocumentScanner tokens = formatter.getTokens();

        for (int i = 0, slc_nls = 0; i < tokens.size(); i += 1) {
            Token token = tokens.get(i);

            if (ignoreToken.contains(token)) {
                continue;
            }

            int tokenType = token.getType();
            if (tokenType == GroovyTokenTypeBridge.SL_COMMENT) {
                slc_nls = 1;
            } else if (tokenType == GroovyTokenTypeBridge.NLS) {
                slc_nls = (slc_nls == 1 ? 2 : 0); // first NLS is required
            } else if (tokenType == GroovyTokenTypeBridge.LCURLY && slc_nls == 0) {
                // single-line closures should not be reformatted
                ClosureExpression maybeClosure = formatter.findCorrespondingClosure(token);
                if (maybeClosure == null || maybeClosure.getLineNumber() != maybeClosure.getLastLineNumber()) {
                    addEdit(lCurlyEditor.apply(i, token), edits);

                    // ensure a newline exists after the "{" token...
                    ASTNode node = formatter.findCorrespondingNode(token);
                    // this rule does not apply for closures, which have their own formatting logic. Note that
                    // ArgumentListExpression is included because when an argument list expression is returned for
                    // a "{" this means it is a "special" argument list without any "()" and just one closure in it.
                    if (node == null || !(node instanceof ClosureExpression || node instanceof CastExpression || node instanceof ArgumentListExpression)) {
                        Token nextToken = tokens.getNextToken(token);
                        if (nextToken != null) {
                            int type = nextToken.getType();
                            if (type != GroovyTokenTypeBridge.EOF && type != GroovyTokenTypeBridge.NLS && type != GroovyTokenTypeBridge.RCURLY) {
                                int offset = tokens.getEnd(token), length = tokens.getOffset(nextToken) - offset;
                                addEdit(new ReplaceEdit(offset, length, formatter.getNewLine()), edits);
                            }
                        }
                    }
                }
            } else if (tokenType == GroovyTokenTypeBridge.RCURLY && slc_nls == 0) {
                Token previousToken = tokens.getLastTokenBefore(token);
                // for cases like method() {} we want the braces to stay where they are
                if (previousToken.getType() != GroovyTokenTypeBridge.LCURLY) {
                    addEdit(rCurlyEditor.apply(i, token), edits);
                }
            } else {
                slc_nls = 0;
            }
        }
    }

    private BiFunction<Integer, Token, TextEdit> getBracesEditor(int style) {
        if (style == PreferenceConstants.NEXT_LINE) {
            return (index, token) -> {
                try {
                    Token lastNonNLS = formatter.getPreviousToken(index);
                    int offset = formatter.getOffsetOfTokenEnd(lastNonNLS);
                    int length = formatter.getOffsetOfToken(token) - offset;
                    return new ReplaceEdit(offset, length, formatter.getNewLine());
                } catch (BadLocationException e) {
                    Util.log(e);
                    return null;
                }
            };
        }
        if (style == PreferenceConstants.SAME_LINE) {
            return (index, token) -> {
                if (formatter.getPreviousTokenIncludingNLS(index).getType() == GroovyTokenTypeBridge.NLS) {
                    try {
                        Token lastNonNLS = formatter.getPreviousToken(index);
                        int offset = formatter.getOffsetOfTokenEnd(lastNonNLS);
                        int length = formatter.getOffsetOfToken(token) - offset;
                        return new ReplaceEdit(offset, length, " "); // check space preference
                    } catch (BadLocationException e) {
                        Util.log(e);
                    }
                }
                return null;
            };
        }
        return null;
    }

    private void removeUnnecessarySemicolons(MultiTextEdit edits) {
        if (preferences.isRemoveUnnecessarySemicolons()) {
            new SemicolonRemover(formatter.selection, formatter.document, edits).format();
        }
    }

    private void addEdit(TextEdit edit, TextEdit container) {
        if (edit != null &&
            edit.getOffset() >= formatter.formatOffset &&
            edit.getOffset() + edit.getLength() <= formatter.formatOffset + formatter.formatLength) {
            if (DEBUG_EDITS) {
                // print out where this edit is taking place
                try {
                    IDocument doc = formatter.getProgressDocument();
                    System.out.println(">>> edit: " + edit);
                    int startLine = doc.getLineOfOffset(edit.getOffset());
                    int endLine = doc.getLineOfOffset(edit.getOffset() + edit.getLength());
                    for (int line = startLine - 1; line < endLine + 1; line += 1) {
                        if (line >= 0 && line < doc.getNumberOfLines()) {
                            for (int i = doc.getLineOffset(line); i < doc.getLineOffset(line) + doc.getLineLength(line); i += 1) {
                                if (i == edit.getOffset())
                                    System.out.print("|>");
                                if (i == edit.getOffset() + edit.getLength())
                                    System.out.print("<|");
                                System.out.print(doc.getChar(i));
                            }
                        }
                    }
                    System.out.println("<<< edit: " + edit);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
            try {
                container.addChild(edit);
            } catch (MalformedTreeException e) {
                //Swallow:
                // This will cause later edits that conflict with earlier ones to be ignored.
                // Can use this to "prioritise" edits generated by different formatting components.
                // Put the formatting components you want to have priority earlier in the call sequence.
                if (DEBUG_EDITS)
                    System.out.println("Last edit was ignored: " + e.getMessage());
            }
        }
    }
}
