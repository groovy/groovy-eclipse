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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovyjarjarantlr.Token;
import org.codehaus.groovy.antlr.GroovyTokenTypeBridge;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.jdt.groovy.core.util.GroovyCodeVisitorAdapter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class GroovyIndentation {

    private static boolean DEBUG; // TODO: Read value using Platform.getDebugOption

    private static void debug(String msg) {
        if (DEBUG) System.out.println(msg);
    }

    private final DefaultGroovyFormatter formatter;
    private final IFormatterPreferences pref;

    private int indentation;
    private final int[] tempIndentation;

    private final LineIndentations lineInd;

    TextEdit indentationEdits;

    private final KlenkDocumentScanner tokens;

    public GroovyIndentation(DefaultGroovyFormatter formatter, IFormatterPreferences pref, int indentationLevel) {
        this.formatter = formatter;
        this.lineInd = new LineIndentations(formatter.getProgressDocument().getNumberOfLines());
        this.tempIndentation = new int[formatter.getProgressDocument().getNumberOfLines()];
        this.indentation = indentationLevel;
        this.tokens = formatter.getTokens();
        this.pref = pref;
    }

    public TextEdit getIndentationEdits() {
        indentationEdits = new MultiTextEdit();

        // GRECLIPSE-1478
        handleMultilineMethodParameters();

        try {
            Token firstToken = tokens.get(0);
            if (formatter.isMultilineStatement(firstToken)) {
                lineInd.setMultilineToken(firstToken.getLine(), firstToken);
                setAdditionalIndentation(firstToken, pref.getIndentationMultiline(), false);
            }

            for (int i = 0, n = tokens.size(); i < n; i += 1) {
                Token token = tokens.get(i);
                int ttype = token.getType();
                int tokenOffset = formatter.getOffsetOfToken(token);
                Token nextToken = formatter.getNextTokenIncludingNLS(i);
                int nextTokenOffset = formatter.getOffsetOfToken(nextToken);

                if (ttype == GroovyTokenTypeBridge.LCURLY || ttype == GroovyTokenTypeBridge.LBRACK) {
                    indentation += 1;
                } else if (ttype == GroovyTokenTypeBridge.RCURLY || ttype == GroovyTokenTypeBridge.RBRACK) {
                    indentation -= 1;
                } else if (ttype == GroovyTokenTypeBridge.LITERAL_else) {
                    Token nextNonNLS = formatter.getNextToken(i);
                    // indent else expression unless it's a same-line "else if"
                    if (nextNonNLS.getType() != GroovyTokenTypeBridge.LCURLY &&
                            (nextNonNLS.getType() != GroovyTokenTypeBridge.LITERAL_if || nextNonNLS.getLine() > token.getLine())) {
                        setAdditionalIndentation(nextNonNLS);
                    }
                } else if (ttype == GroovyTokenTypeBridge.LITERAL_if || ttype == GroovyTokenTypeBridge.LITERAL_for ||
                        ttype == GroovyTokenTypeBridge.LITERAL_while /*|| ttype == GroovyTokenTypeBridge.LITERAL_do*/) {
                    ASTNode condition = getConditionExpression(token);
                    if (condition != null) { // set indent for "if/for/while (...)" where paren stuff spans lines
                        for (int line = condition.getLineNumber() + 1, last = condition.getLastLineNumber(); line <= last; line += 1) {
                            tempIndentation[line - 1] += pref.getIndentationMultiline();
                            lineInd.setMultilineIndentation(line, true);
                        }
                    }
                    setAdditionalIndentation(formatter.getTokenAfterParenthesis(i));
                } else if (ttype == GroovyTokenTypeBridge.LITERAL_switch) {
                    indentendSwitchStatement(token);
                } else if (ttype == GroovyTokenTypeBridge.EOF || ttype == GroovyTokenTypeBridge.NLS) {
                    if (nextToken.getType() == GroovyTokenTypeBridge.RCURLY || nextToken.getType() == GroovyTokenTypeBridge.RBRACK) {
                        tempIndentation[token.getLine()] -= 1;
                    }
                    deleteWhiteSpaceBefore(token);
                    if (ttype != GroovyTokenTypeBridge.EOF) {
                        int offsetAfterNLS = tokenOffset + formatter.getProgressDocument().getLineDelimiter(token.getLine() - 1).length();
                        if (!isEmptyLine(token.getLine()) || formatter.pref.isIndentEmptyLines()) {
                            addEdit(new ReplaceEdit(offsetAfterNLS, (nextTokenOffset - offsetAfterNLS),
                                    formatter.getLeadingGap(indentation + tempIndentation[token.getLine()])));
                        }
                        lineInd.setLineIndentation(token.getLine() + 1, indentation + tempIndentation[token.getLine()]);

                        if (formatter.isMultilineStatement(nextToken)) {
                            lineInd.setMultilineToken(token.getLine(), token);
                            setAdditionalIndentation(nextToken, pref.getIndentationMultiline(), false);
                        }
                    }
                } else if (ttype == GroovyTokenTypeBridge.ML_COMMENT) {
                    addEdit(new ReplaceEdit(tokenOffset, (nextTokenOffset - tokenOffset),
                        formatMultilineComment(formatter.getProgressDocument().get(tokenOffset, (nextTokenOffset - tokenOffset)), indentation)));
                }
            }
        } catch (BadLocationException e) {
            GroovyCore.logException("Exception thrown while determining indentation", e);
        }
        return indentationEdits;
    }

    private ASTNode getConditionExpression(final Token token) {
        ASTNode node = formatter.findCorrespondingNode(token);
        if (node != null) {
            ASTNode[] parenBlock = new ASTNode[1];
            node.visit(new GroovyCodeVisitorAdapter() {
                @Override
                public void visitIfElse(final IfStatement statement) {
                    parenBlock[0] = statement.getBooleanExpression();
                }

                @Override
                public void visitForLoop(final ForStatement statement) {
                    parenBlock[0] = statement.getCollectionExpression();
                }

                @Override
                public void visitWhileLoop(final WhileStatement statement) {
                    parenBlock[0] = statement.getBooleanExpression();
                }

                @Override
                public void visitDoWhileLoop(final DoWhileStatement statement) {
                    parenBlock[0] = statement.getBooleanExpression();
                }
            });
            return parenBlock[0];
        }
        return null;
    }

    // GRECLIPSE-1478 and GRECLIPSE-1508 add proper indentation for methods with
    // multiline parameters
    private void handleMultilineMethodParameters() {
        // for each real method node, add indentation for lines that contain
        // method parameters
        // that are not on the same line as the method start.
        ModuleNode rootNode = formatter.getProgressRootNode();
        List<ClassNode> classes = rootNode.getClasses();
        int indentationMultiline = pref.getIndentationMultiline();
        for (ClassNode classNode : classes) {
            List<MethodNode> methods = classNode.getMethods();
            for (MethodNode method : methods) {
                if (method.getEnd() > 1 && method.getParameters() != null && method.getParameters().length > 0) {
                    Parameter[] ps = method.getParameters();
                    Statement code = method.getCode();
                    Parameter lastP = ps[ps.length - 1];

                    // the line start is the line that contains the opening paren of the parameters
                    int maybeMethodStart = (method.getAnnotations() != null && !method.getAnnotations().isEmpty())
                        ? method.getAnnotations().get(method.getAnnotations().size() - 1).getEnd() : method.getStart();
                    List<Token> methodTokens = tokens.getTokens(maybeMethodStart, method.getParameters()[0].getStart());

                    int lineStart = method.getLineNumber();
                    for (int i = methodTokens.size() - 1; i >= 0; i--) {
                        Token token = methodTokens.get(i);
                        if (token.getType() == GroovyTokenTypeBridge.LPAREN) {
                            lineStart = token.getLine();
                            break;
                        }
                    }

                    int lineEnd = code != null ? code.getLineNumber() : lastP.getLastLineNumber();
                    if (lineStart != lineEnd) {
                        // now determine if we need to also indent the last line
                        // it might just be an lparen or an rcurly. In that
                        // case, don't indent
                        // if there is real text on it, do indent
                        // can't compare AST nodes directly since the last
                        // parameter will
                        // include trailing newline if the last paren is on a
                        // separate line
                        // instead find the closing paren after the last param
                        // and see if on same line
                        int tokenIndex = tokens.findTokenFrom(lastP.getEnd());
                        Token lastParamToken = tokens.get(tokenIndex - 1);
                        Token openingBracket = null;
                        while (++tokenIndex < tokens.size()) {
                            openingBracket = tokens.get(tokenIndex);
                            if (openingBracket.getType() == GroovyTokenTypeBridge.LCURLY) {
                                break;
                            }
                        }
                        boolean doLastLineIndent = openingBracket != null && lastParamToken.getLine() == openingBracket.getLine();
                        for (int i = lineStart + 1; i < lineEnd; i++) {
                            tempIndentation[i - 1] += indentationMultiline;
                        }
                        if (doLastLineIndent) {
                            tempIndentation[lineEnd - 1] += indentationMultiline;
                        }
                    }
                }
            }
        }
    }

    /**
     * @param Zero-based line number in the formattedDocument
     * @return Whether the line in the document contains only whitespace.
     */
    private boolean isEmptyLine(int line) {
        try {
            IDocument d = formatter.getProgressDocument();
            int lineStart = d.getLineOffset(line);
            int lineLen = d.getLineLength(line);
            String lineTxt = d.get(lineStart, lineLen);
            boolean result = lineTxt.trim().equals("");
            // debug("isEmptyLine(" + line + ") txt = '" + lineTxt + "'" + "=>"
            // + result);
            return result;
        } catch (BadLocationException e) {
            return true; // Presumably the line is outside the document so its empty by definition
        }
    }

    /**
     * Create and add an edit to remove any whitespace (excluding newlines)
     * before the given token.
     *
     * @throws BadLocationException
     */
    private void deleteWhiteSpaceBefore(Token token) throws BadLocationException {
        int endPos = tokens.getOffset(token);
        int startPos = endPos;
        IDocument d = formatter.getProgressDocument();
        while (startPos > 0 && isTabOrSpace(d.getChar(startPos - 1))) {
            startPos -= 1;
        }
        // Complication, we shouldn't do this if "indent empty lines" is true
        // and this is an empty line
        // because then the delete edit will conflict with the edit to create
        // the indentation.
        if (!formatter.pref.isIndentEmptyLines() || !isEmptyLine(formatter.getProgressDocument().getLineOfOffset(startPos))) {
            addEdit(new DeleteEdit(startPos, endPos - startPos));
        }
    }

    private static boolean isTabOrSpace(char c) {
        return c == ' ' || c == '\t';
    }

    private void indentendSwitchStatement(Token token) {
        if (token != null) {
            ASTNode node = formatter.findCorrespondingNode(token);
            if (node instanceof SwitchStatement) {
                SwitchStatement switchstmt = (SwitchStatement) node;
                for (CaseStatement cs : switchstmt.getCaseStatements()) {
                    indentendBlockStatement(cs.getCode(), cs.getLineNumber());
                }
                // Hack because the default statement has wrong line infos
                Statement defaultstmt = switchstmt.getDefaultStatement();
                int posDef = formatter.getPosOfToken(defaultstmt.getLineNumber(), defaultstmt.getColumnNumber());
                if (posDef != -1) {
                    Token prev = formatter.getPreviousToken(posDef);
                    indentendBlockStatement(switchstmt.getDefaultStatement(), prev.getLine());
                }
            }
        }
    }

    private void indentendBlockStatement(Statement stmt, int currentLine) {
        if (stmt instanceof BlockStatement) {
            BlockStatement defaultBlock = (BlockStatement) stmt;
            for (Statement sm : defaultBlock.getStatements()) {
                if (sm.getLineNumber() > currentLine) {
                    for (int i = sm.getLineNumber(); i <= sm.getLastLineNumber(); i++) {
                        tempIndentation[i - 1] += 1;
                    }
                }
            }
        }
    }

    private void addEdit(TextEdit edit) {
        if (edit instanceof DeleteEdit && edit.getLength() == 0) {
            return;
        }
        if (edit instanceof ReplaceEdit && edit.getLength() == 0 && ((ReplaceEdit) edit).getText().length() < 1) {
            return;
        }
        if (edit instanceof InsertEdit && ((InsertEdit) edit).getText().length() < 1) {
            return;
        }
        if (edit != null && edit.getOffset() >= formatter.formatOffset &&
                edit.getOffset() + edit.getLength() <= formatter.formatOffset + formatter.formatLength) {
            if (edit instanceof DeleteEdit) {
                debug("DeleteEdit: " + edit.getOffset() + ":" + edit.getLength());
                debug("---------------------------");
                IDocument doc = formatter.getProgressDocument();
                try {
                    debug(doc.get(0, edit.getOffset()) +
                        "|*>" + doc.get(edit.getOffset(), edit.getLength()) +
                        "<*|" + doc.get(edit.getOffset() + edit.getLength(), doc.getLength() - (edit.getOffset() + edit.getLength()))
                    );
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                debug("---------------------------");
            }
            try {
                indentationEdits.addChild(edit);
            } catch (MalformedTreeException e) {
                debug("Ignored conflicting edit: " + edit);
                GroovyCore.logException("WARNING: Formatting ignored a conflicting text edit", e);
            }
        }
    }

    private void setAdditionalIndentation(Token token, int indent, boolean firstLineInlcuded) throws BadLocationException {
        if (token != null) {

            // Skipping (but indenting) single-line comments
            while (token.getType() == GroovyTokenTypeBridge.SL_COMMENT) {
                tempIndentation[token.getLine() - 1] += indent;
                token = formatter.getNextToken(formatter.getPosOfToken(token));
            }
            if (token.getType() != GroovyTokenTypeBridge.LCURLY) {
                ASTNode node = formatter.findCorrespondingNode(token);
                if (node != null) {
                    int lineNumber = node.getLineNumber();
                    if (!firstLineInlcuded) {
                        lineNumber++;
                    }
                    for (; lineNumber <= node.getLastLineNumber(); lineNumber++) {
                        if (isLastClosureArg(lineNumber - 1, node))
                            break;
                        tempIndentation[lineNumber - 1] += indent;
                        lineInd.setMultilineIndentation(lineNumber, true);
                    }
                }
            }
        }
    }

    private void setAdditionalIndentation(Token t) throws BadLocationException {
        setAdditionalIndentation(t, 1, true);
    }

    public LineIndentations getLineIndentations() {
        return lineInd;
    }

    /**
     * Tests whether a given line (0-base index) is the start of a
     * "last closure" argument.
     *
     * @param line
     * @param node The parent node of which this might be a last closure
     *            argument.
     */
    private boolean isLastClosureArg(int line, ASTNode node) {
        try {
            Token token = tokens.getTokenFrom(tokens.getDocument().getLineOffset(line));
            if (token == null)
                return false;
            else if ("{".equals(token.getText())) {
                ASTNode nestedNode = formatter.findCorrespondingNode(token);
                return node.getEnd() == nestedNode.getEnd();
            }
            return false;
        } catch (Throwable e) {
            GroovyCore.logException("internal error", e);
            return false;
        }
    }

    /**
     * Format a multi line Comment
     *
     * @param string
     *            the Coment to format
     * @param ind
     *            the current indentation level
     * @return the formatted indeationed comment
     * @throws BadLocationException
     */
    private String formatMultilineComment(String str, int ind) throws BadLocationException {
        String string = str;
        Matcher m = Pattern.compile("(\n|\r|\r\n)\\s*", Pattern.MULTILINE).matcher(string);
        string = m.replaceAll(formatter.getNewLine() + formatter.getLeadingGap(ind) + " ");
        return string;
    }
}
