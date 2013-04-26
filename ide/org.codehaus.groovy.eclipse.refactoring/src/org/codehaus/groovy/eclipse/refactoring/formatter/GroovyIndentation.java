/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import groovyjarjarantlr.Token;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.greclipse.GroovyTokenTypeBridge;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Mike Klenk mklenk@hsr.ch
 * @author kdvolder
 */
public class GroovyIndentation {

    private static boolean DEBUG = false;

    private void debug(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

	private final DefaultGroovyFormatter formatter;
	private final IFormatterPreferences pref;

	private int indentation = 0;
	private final int[] tempIndentation;

	private final LineIndentations lineInd;

	TextEdit indentationEdits;

    private final KlenkDocumentScanner tokens;

	public GroovyIndentation(DefaultGroovyFormatter formatter,
			IFormatterPreferences pref, int indentationLevel) {
		this.formatter = formatter;
		tempIndentation = new int[formatter.getProgressDocument().getNumberOfLines()];
		lineInd = new LineIndentations(formatter.getProgressDocument().getNumberOfLines());

		this.tokens = formatter.getTokens();
		this.pref = pref;
		this.indentation = indentationLevel;
	}

	public TextEdit getIndentationEdits() {
		indentationEdits = new MultiTextEdit();

        // GRECLIPSE-1478
        handleMultilineMethodParameters();

		try {
			if (formatter.isMultilineStatement(tokens.get(0))) {
				setAdditionalIndentation(tokens.get(0),
						pref.getIndentationMultiline(), false);
				lineInd.setMultilineToken(tokens.get(0).getLine(), tokens.get(0));
			}

			Token token = null;
            for (int i = 0; i < tokens.size(); i++) {
				token = tokens.get(i);
				int offsetToken = formatter.getOffsetOfToken(token);
				int offsetNextToken = formatter.getOffsetOfToken(formatter
						.getNextTokenIncludingNLS(i));


                int ttype = token.getType();
                // can't use a switch here since the values are not constants
                if (ttype == GroovyTokenTypeBridge.LITERAL_if || ttype == GroovyTokenTypeBridge.LITERAL_while
                        || ttype == GroovyTokenTypeBridge.LITERAL_for) {
                    setAdditionalIndentation(formatter.getTokenAfterParenthesis(i));
                } else if (ttype == GroovyTokenTypeBridge.LCURLY || ttype == GroovyTokenTypeBridge.LBRACK) {
                    indentation++;
                } else if (ttype == GroovyTokenTypeBridge.LITERAL_switch) {
                    indentendSwitchStatement(token);
                } else if (ttype == GroovyTokenTypeBridge.RCURLY || ttype == GroovyTokenTypeBridge.RBRACK) {
                    indentation--;
                } else if (ttype == GroovyTokenTypeBridge.LITERAL_else) {
                    int nextToken = formatter.getNextToken(i).getType();
                    // adding indentation when there is no opening and it is
                    // not an "else if" construct
                    if (nextToken != GroovyTokenTypeBridge.LCURLY && nextToken != GroovyTokenTypeBridge.LITERAL_if) {
                        setAdditionalIndentation(formatter.getNextToken(i));
                    }
                } else if (ttype == GroovyTokenTypeBridge.EOF || ttype == GroovyTokenTypeBridge.NLS) {
                    int nextTokenType = formatter.getNextTokenIncludingNLS(i).getType();
                    if (nextTokenType == GroovyTokenTypeBridge.RCURLY || nextTokenType == GroovyTokenTypeBridge.RBRACK) {
                        tempIndentation[token.getLine()]--;
                    }
                    deleteWhiteSpaceBefore(token);
                    if (ttype != GroovyTokenTypeBridge.EOF) {
                        Token nextMultiToken = formatter.getNextTokenIncludingNLS(i);
                        int offsetAfterNLS = offsetToken
                                + formatter.getProgressDocument().getLineDelimiter(token.getLine() - 1).length();
                        if (!isEmptyLine(token.getLine()) || formatter.pref.isIndentEmptyLines()) {
                            addEdit(new ReplaceEdit(offsetAfterNLS, (offsetNextToken - offsetAfterNLS),
                                    formatter.getLeadingGap(indentation + tempIndentation[token.getLine()])));
                        }
                        lineInd.setLineIndentation(token.getLine() + 1, indentation + tempIndentation[token.getLine()]);

                        if (formatter.isMultilineStatement(nextMultiToken)) {
                            setAdditionalIndentation(nextMultiToken, pref.getIndentationMultiline(), false);
                            lineInd.setMultilineToken(token.getLine(), token);
                        }
                    }
                } else if (ttype == GroovyTokenTypeBridge.ML_COMMENT) {
                    addEdit(new ReplaceEdit(offsetToken, (offsetNextToken - offsetToken), formatMultilineComment(formatter
                            .getProgressDocument().get(offsetToken, (offsetNextToken - offsetToken)), indentation)));
                }
            }
		} catch (BadLocationException e) {
            GroovyCore.logException("Exception thrown while determining indentation", e);
		}
		return indentationEdits;
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

                    // the line start is the line that contains the opening
                    // paren of the parameters
                    // This is not directly in the ast, must search
                    int maybeMethodStart = (method.getAnnotations() != null && method.getAnnotations().size() > 0) ? method
                            .getAnnotations().get(method.getAnnotations().size() - 1).getEnd() : method.getStart();

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
            return true; // Presumably the line is outside the document so its
                         // empty by definition
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
            startPos--;
        }
        // Complication, we shouldn't do this if "indent empty lines" is true
        // and this is an empty line
        // because then the delete edit will conflict with the edit to create
        // the indentation.
        if (!formatter.pref.isIndentEmptyLines() || !isEmptyLine(formatter.getProgressDocument().getLineOfOffset(startPos))) {
            addEdit(new DeleteEdit(startPos, endPos - startPos));
        }
    }

    private boolean isTabOrSpace(char c) {
        return c == ' ' || c == '\t';
    }

    private void indentendSwitchStatement(Token token) {
		if (token != null) {
			ASTNode node = formatter.findCorrespondingNode(token);
			if (node instanceof SwitchStatement) {
				SwitchStatement switchstmt = (SwitchStatement) node;
				for(CaseStatement cs : (List<CaseStatement>)switchstmt.getCaseStatements()) {
					indentendBlockStatement(cs.getCode(), cs.getLineNumber());
				}
				// Hack because the default statement has wrong line infos
				Statement defaultstmt = switchstmt.getDefaultStatement();
				int posDef = formatter.getPosOfToken(defaultstmt.getLineNumber(), defaultstmt.getColumnNumber());
				if(posDef != -1) {
					Token def = formatter.getPreviousToken(posDef);
					indentendBlockStatement(switchstmt.getDefaultStatement(),def.getLine());
				}
			}
		}
	}

	private void indentendBlockStatement(Statement stmt, int currentLine) {
		if (stmt instanceof BlockStatement) {
			BlockStatement defaultBlock = (BlockStatement) stmt;
			for(Statement sm : (List<Statement>)defaultBlock.getStatements()) {
				if(sm.getLineNumber() > currentLine) {
					for(int i = sm.getLineNumber(); i <= sm.getLastLineNumber(); i++){
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
        if (edit instanceof ReplaceEdit && edit.getLength() == 0 && ((ReplaceEdit) edit).getText().isEmpty()) {
            return;
        }
        if (edit instanceof InsertEdit && ((InsertEdit) edit).getText().isEmpty()) {
            return;
        }
		if(edit != null && edit.getOffset() >= formatter.formatOffset &&
				edit.getOffset() + edit.getLength() <= formatter.formatOffset + formatter.formatLength) {
            if (edit instanceof DeleteEdit) {
                debug("DeleteEdit: " + edit.getOffset() + ":" + edit.getLength());
                debug("---------------------------");
                IDocument doc = formatter.getProgressDocument();
                try {
                    debug(doc.get(0, edit.getOffset())
                            + "|*>" + doc.get(edit.getOffset(), edit.getLength()) + "<*|"
                            + doc.get(edit.getOffset() + edit.getLength(), doc.getLength() - (edit.getOffset() + edit.getLength())));
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

	private void setAdditionalIndentation(Token t, int i,
			boolean firstLineInlcuded) {
        if (t != null) {
            if (t.getType() != GroovyTokenTypeBridge.LCURLY) {
                ASTNode node = formatter.findCorrespondingNode(t);
                if (node != null) {
                    int r = node.getLineNumber();
                    if (!firstLineInlcuded) {
                        r++;
                    }
                    for (; r <= node.getLastLineNumber(); r++) {
                        if (isLastClosureArg(r - 1, node))
                            break;
                        tempIndentation[r - 1] += i;
                        lineInd.setMultilineIndentation(r, true);
                    }
                }
            }
        }
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

    private void setAdditionalIndentation(Token t) {
		setAdditionalIndentation(t, 1, true);
	}

	public LineIndentations getLineIndentations() {
		return lineInd;
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
	private String formatMultilineComment(String str, int ind)
			throws BadLocationException {
	    String string = str;
		Matcher m = Pattern.compile("(\n|\r|\r\n)\\s*", Pattern.MULTILINE)
				.matcher(string);
		string = m.replaceAll(formatter.getNewLine() + formatter.getLeadingGap(ind) + " ");
		return string;
	}
}