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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.greclipse.GroovyTokenTypeBridge;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.PreferenceConstants;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTScanner;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.ClosuresInCodePredicate;
import org.codehaus.groovy.eclipse.refactoring.formatter.lineWrap.CorrectLineWrap;
import org.codehaus.groovy.eclipse.refactoring.formatter.lineWrap.NextLine;
import org.codehaus.groovy.eclipse.refactoring.formatter.lineWrap.SameLine;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Mike Klenk mklenk@hsr.ch
 */
public class GroovyBeautifier {

    private static final boolean DEBUG_EDITS = false;

    public DefaultGroovyFormatter formatter;
	private final IFormatterPreferences preferences;
	private final Set<Token> ignoreToken;

    public GroovyBeautifier(DefaultGroovyFormatter defaultGroovyFormatter, IFormatterPreferences pref) {
		this.formatter = defaultGroovyFormatter;
		this.preferences = pref;
		ignoreToken = new HashSet<Token>();
	}

	public TextEdit getBeautifiEdits() throws MalformedTreeException, BadLocationException {
		MultiTextEdit edits = new MultiTextEdit();

        combineClosures(edits);
        formatLists(edits);
		correctBraces(edits);
        removeUnnecessarySemicolons(edits);

        formatter.getTokens().dispose();
        return edits;
	}

    private void formatLists(MultiTextEdit edits) {
        ASTScanner scanner = new ASTScanner(formatter.getProgressRootNode(), new ListInCodePredicate(),
                formatter.getProgressDocument());
        scanner.startASTscan();
        for(ASTNode _node : scanner.getMatchedNodes().keySet()) {
            ListExpression node = ((ListExpression)_node);

            GroovyDocumentScanner tokens = formatter.getTokens();
            Token lastToken = null;
            try {
                lastToken = tokens.getLastNonWhitespaceTokenBefore(node.getEnd() - 1);
                if (lastToken == null || lastToken.getType() == GroovyTokenTypeBridge.STRING_CTOR_START) {
                    //This means we are inside a GString and we won't apply edits here so skip this
                    continue;
                }
            } catch (BadLocationException e) {
                Util.log(e);
                continue;
            }

            int nodeStart = node.getStart();
            int nodeEnd = node.getEnd();
            int nodeLen = nodeEnd - nodeStart;
            boolean isLong = nodeLen > preferences.getLongListLength();
            List<Expression> exps = node.getExpressions();

            // GRECLIPSE-1427 if the next token is 'as', then don't add a
            // newline or remove whitespace
            Token maybeAs;
            try {
                maybeAs = tokens.getNextToken(lastToken);
            } catch (BadLocationException e) {
                GroovyCore.logException("Trouble getting next token", e);
                maybeAs = null;
            }
            boolean nextTokenAs = maybeAs != null && maybeAs.getType() == GroovyTokenTypeBridge.LITERAL_as;

            if (isLong || (hasClosureElement(node) && node.getExpressions().size() > 1)) {
                //Split the list
                for (int i = 0; i < exps.size(); i++) {
                    Expression exp = exps.get(i);
                    Token before = tokens.getLastTokenBefore(exp.getStart());
                    try {
                        while (before.getType() != GroovyTokenTypeBridge.LBRACK && before.getType() != GroovyTokenTypeBridge.COMMA) {
                            before = tokens.getLastTokenBefore(before);
                        }
                        replaceWhiteSpaceAfter(edits, before, formatter.getNewLine());
                    } catch (BadLocationException e) {
                        GroovyCore.logException("Trouble formatting list", e);
                    }
                }
                if (!nextTokenAs) {
                    replaceWhiteSpaceAfter(edits, lastToken, formatter.getNewLine());
                }
            }
            else {
                //Compact the list
                for (int i = 0; i < exps.size(); i++) {
                    Expression exp = exps.get(i);
                    Token before = tokens.getLastTokenBefore(exp.getStart());
                    try {
                        while (before.getType() != GroovyTokenTypeBridge.LBRACK && before.getType() != GroovyTokenTypeBridge.COMMA) {
                            before = tokens.getLastTokenBefore(before);
                        }
                        replaceWhiteSpaceAfter(edits, before, before.getType() == GroovyTokenTypeBridge.LBRACK ? "" : " ");
                    } catch (BadLocationException e) {
                        Util.log(e);
                    }
                }
                if (!nextTokenAs) {
                    replaceWhiteSpaceAfter(edits, lastToken,
                            lastToken.getType() == GroovyTokenTypeBridge.SL_COMMENT ? formatter.getNewLine() : "");
                }
            }
        }
    }

    /**
     * Create an edit that replaces whitespace tokens immediately after given
     * token with a String.
     */
    private void replaceWhiteSpaceAfter(MultiTextEdit edits, Token token, String replaceWith) {
        GroovyDocumentScanner tokens = formatter.getTokens();
        try {
            int editStart = tokens.getEnd(token);
            Token first = tokens.getNextToken(token); // First whitespace token (if any)
            Token last = first; // First non-whitespace token
            // If no white space tokens where found then first and last will be
            // the same token
            // (i.e. token just after the given token
            while (isWhiteSpace(last.getType())) {
                last = tokens.getNextToken(last);
            }
            replaceFromTo(editStart, tokens.getOffset(last), replaceWith, edits);
        } catch (BadLocationException e) {
            Util.log(e);
        }
    }

    private boolean isWhiteSpace(int type) {
        return type == GroovyTokenTypeBridge.WS || type == GroovyTokenTypeBridge.NLS;
    }

    private boolean hasClosureElement(ListExpression node) {
        List<Expression> list = node.getExpressions();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ClosureExpression)
                return true;
        }
        return false;
    }

    private void combineClosures(MultiTextEdit edits) throws BadLocationException {
		ASTScanner scanner = new ASTScanner(formatter.getProgressRootNode(),new ClosuresInCodePredicate(),formatter.getProgressDocument());
		scanner.startASTscan();
		for(ASTNode node : scanner.getMatchedNodes().keySet()) {
			ClosureExpression clExp = ((ClosureExpression)node);

            int posClStart = formatter.getPosOfToken(GroovyTokenTypeBridge.LCURLY, clExp.getLineNumber(), clExp.getColumnNumber(),
                    "{");
            if (posClStart == -1) {
                // Skip... invalid (likely the closure is
                // inside a GString so can't find tokens in
                // there.
                continue;
            }

            int posCLEnd = formatter.getPosOfToken(GroovyTokenTypeBridge.RCURLY, clExp.getLastLineNumber(),
                    clExp.getLastColumnNumber() - 1, "}");

			if(posCLEnd == -1) {
				int positionLastTokenOfClosure = formatter.getPosOfToken(clExp.getLastLineNumber(), clExp.getLastColumnNumber());
                while (formatter.getTokens().get(positionLastTokenOfClosure).getType() != GroovyTokenTypeBridge.RCURLY) {
					positionLastTokenOfClosure--;
				}
				posCLEnd = positionLastTokenOfClosure;
			}
			// Ignore closure on one Line
			if(clExp.getLineNumber() == clExp.getLastLineNumber()) {
				ignoreToken.add(formatter.getTokens().get(posCLEnd));
				continue;
			}

			if (clExp.getCode() instanceof BlockStatement) {
				BlockStatement codeblock = (BlockStatement) clExp.getCode();
				int posParamDelim = posClStart;
				if(clExp.getParameters() != null && clExp.getParameters().length > 0) {
					// Position Parameters on same Line
                    posParamDelim = formatter.getPosOfNextTokenOfType(posClStart, GroovyTokenTypeBridge.CLOSABLE_BLOCK_OP);
					replaceNLSWithSpace(edits, posClStart, posParamDelim);
				}
				// combine closure with only one statments with less than 5 tokens to one line
				if(codeblock.getStatements().size() == 1 && (posCLEnd - posClStart) < 10) {
                    replaceNLSWithSpace(edits, posParamDelim, posCLEnd);
                    ignoreToken.add(formatter.getTokens().get(posCLEnd));
				} else {
                    // check if there is a linebreak after the parameters
                    if (posParamDelim > 0
                            && formatter.getNextTokenIncludingNLS(posParamDelim).getType() != GroovyTokenTypeBridge.NLS) {
                        addEdit(new InsertEdit(formatter.getOffsetOfTokenEnd(formatter.getTokens().get(posParamDelim)), formatter
                                .getNewLine()), edits);
                    } else {
                        // If there are no parameters check if the first
                        // statement
                        // is on the next line
                        if (posParamDelim == 0
                                && formatter.getNextTokenIncludingNLS(posClStart).getType() != GroovyTokenTypeBridge.NLS) {
                            addEdit(new InsertEdit(formatter.getOffsetOfTokenEnd(formatter.getTokens().get(posClStart)), formatter
                                    .getNewLine()), edits);
                        }
                    }
                }
			}
		}
	}

	private void replaceNLSWithSpace(MultiTextEdit container, int startPos,
			int endPos) throws BadLocationException {
        Token fromToken = null; // remember first NLS token in a string of NLS
                                // tokens
        int p = startPos + 1;
        while (p < endPos) {
            Token token = formatter.getTokens().get(p);
            int ttype = token.getType();
            if (ttype == GroovyTokenTypeBridge.NLS) {
                if (fromToken == null)
                    fromToken = token;
            } else {
                if (ttype == GroovyTokenTypeBridge.SL_COMMENT) {
                    ++p; // next token will be skipped whether it is a NLS or
                         // not!
                }
                if (fromToken != null) {
                    // replace NLS tokens from fromToken up to current token
                    replaceFromTo(fromToken, token, " ", container);
                    fromToken = null;
                }
            }
            ++p;
		}
        // don't forget to replace nls tokens at the end.
        if (fromToken != null) {
            Token token = formatter.getTokens().get(p);
            replaceFromTo(fromToken, token, " ", container);
        }
	}

    /**
     * Create an edit that replaces text from the start of fromToken to the
     * start of toToken.
     * The edit is added to container.
     *
     * @param fromToken Where to start replacing text.
     * @param toToken Where to end replacing text.
     * @param with The text to replace the original text with.
     * @param container The container to which the textedit is to be added.
     * @throws BadLocationException
     */
    private void replaceFromTo(Token fromToken, Token toToken, String with, MultiTextEdit container) throws BadLocationException {
        int startEdit = formatter.getOffsetOfToken(fromToken);
        int endEdit = formatter.getOffsetOfToken(toToken);
        addEdit(new ReplaceEdit(startEdit, endEdit - startEdit, with), container);
    }

    private void replaceFromTo(int startEdit, int endEdit, String with, MultiTextEdit container) {
        addEdit(new ReplaceEdit(startEdit, endEdit - startEdit, with), container);
    }

    private void correctBraces(MultiTextEdit edits) throws BadLocationException {
		CorrectLineWrap lCurlyCorrector = null;
		CorrectLineWrap rCurlyCorrector = null;
        if (preferences.getBracesStart() == PreferenceConstants.SAME_LINE) {
            lCurlyCorrector = new SameLine(this);
        } else if (preferences.getBracesStart() == PreferenceConstants.NEXT_LINE) {
            lCurlyCorrector = new NextLine(this);
        }
        if (preferences.getBracesEnd() == PreferenceConstants.SAME_LINE) {
            rCurlyCorrector = new SameLine(this);
        } else if (preferences.getBracesEnd() == PreferenceConstants.NEXT_LINE) {
            rCurlyCorrector = new NextLine(this);
        }

		assert lCurlyCorrector != null;
		assert rCurlyCorrector != null;

		Token token;
		boolean skipNextNLS = false;
		for (int i = 0; i < formatter.getTokens().size(); i++) {

			token = formatter.getTokens().get(i);
			if(ignoreToken.contains(token))
				continue;

            int ttype = formatter.getTokens().get(i).getType();
            if (ttype == GroovyTokenTypeBridge.LCURLY) {
                KlenkDocumentScanner tokens = formatter.getTokens();
                if (skipNextNLS) {
                    skipNextNLS = false;
                    break;
                }

                // single line closures should not be reformatted like this
                ClosureExpression maybeClosure = formatter.findCorrespondingClosure(token);
                if (maybeClosure ==  null
                        || maybeClosure.getLineNumber() != maybeClosure.getLastLineNumber()) {
                    addEdit(lCurlyCorrector.correctLineWrap(i, token), edits);

                    // Ensure a newline exists after the "{" token...
                    ASTNode node = formatter.findCorrespondingNode(token);
                    if (node == null || !(node instanceof ClosureExpression || node instanceof ArgumentListExpression)) {
                        // this rule doesn't apply for closures which have their
                        // own formatting logic. Note that
                        // ArgumentListExpression is included because when an
                        // argument list expression
                        // is returned for a "{" this means it is a "special"
                        // argument list without any "()" and just one closure
                        // in it.
                        Token nextToken = tokens.getNextToken(token);
                        if (nextToken != null) {
                            int type = nextToken.getType();
                            if (type != GroovyTokenTypeBridge.NLS) {
                                int start = tokens.getEnd(token);
                                int end = tokens.getOffset(nextToken);
                                addEdit(new ReplaceEdit(start, end - start, formatter.getNewLine()), edits);
                            }
                        }
                    }
                }

            } else if (ttype == GroovyTokenTypeBridge.RCURLY) {
                if (skipNextNLS) {
                    skipNextNLS = false;
                } else {
                    addEdit(rCurlyCorrector.correctLineWrap(i, token), edits);
                }
            } else if (ttype == GroovyTokenTypeBridge.NLS) {
                // nothing
            } else if (ttype == GroovyTokenTypeBridge.SL_COMMENT) {
                skipNextNLS = true;
			}
		}
	}

    private void removeUnnecessarySemicolons(MultiTextEdit edits) throws BadLocationException {
        if (preferences.isRemoveUnnecessarySemicolons()) {
            GroovyFormatter semicolonRemover = new SemicolonRemover(formatter.selection, formatter.document, edits);
            semicolonRemover.format();
        }
    }

	private void addEdit(TextEdit edit,TextEdit container) {
		if (edit != null && edit.getOffset() >= formatter.formatOffset &&
				edit.getOffset() + edit.getLength() <= formatter.formatOffset + formatter.formatLength) {
            if (DEBUG_EDITS) {
                // print out where this edit is taking place
                try {
                    IDocument doc = formatter.getProgressDocument();
                    System.out.println(">>> edit: " + edit);
                    int startLine = doc.getLineOfOffset(edit.getOffset());
                    int endLine = doc.getLineOfOffset(edit.getOffset() + edit.getLength());
                    for (int line = startLine - 1; line < endLine + 1; line++) {
                        if (line >= 0 && line < doc.getNumberOfLines()) {
                            for (int i = doc.getLineOffset(line); i < doc.getLineOffset(line) + doc.getLineLength(line); i++) {
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
            } // Debug -- end
            try {
                container.addChild(edit);
            }
            catch (MalformedTreeException e) {
                //Swallow:
                // This will cause later edits that conflict with earlier ones to be ignored.
                // Can use this to "prioritise" edits generated by different formatting components.
                // Put the formatting components you want to have priority earlier in the call sequence.
                if (DEBUG_EDITS)
                    System.out.println("Last edit was ignored: "+e.getMessage());
            }
		}
	}

}
