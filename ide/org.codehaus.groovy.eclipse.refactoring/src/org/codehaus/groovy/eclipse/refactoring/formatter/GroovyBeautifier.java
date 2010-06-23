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

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTScanner;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.ClosuresInCodePredicate;
import org.codehaus.groovy.eclipse.refactoring.formatter.lineWrap.CorrectLineWrap;
import org.codehaus.groovy.eclipse.refactoring.formatter.lineWrap.NextLine;
import org.codehaus.groovy.eclipse.refactoring.formatter.lineWrap.SameLine;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import antlr.Token;

/**
 * @author Mike Klenk mklenk@hsr.ch
 *
 */
public class GroovyBeautifier {

    private static final boolean DEBUG_EDITS = false;

    public DefaultGroovyFormatter formatter;
	private final IFormatterPreferences preferences;
	private final Set<Token> ignoreToken;


	public GroovyBeautifier(DefaultGroovyFormatter defaultGroovyFormatter,
			IFormatterPreferences pref) {
		this.formatter = defaultGroovyFormatter;
		this.preferences = pref;
		ignoreToken = new HashSet<Token>();
	}

	public TextEdit getBeautifiEdits() throws MalformedTreeException, BadLocationException {
		MultiTextEdit edits = new MultiTextEdit();

        combineClosures(edits);
		correctBraces(edits);

		return edits;
	}

	private void combineClosures(MultiTextEdit edits) throws BadLocationException {
		ASTScanner scanner = new ASTScanner(formatter.getProgressRootNode(),new ClosuresInCodePredicate(),formatter.getProgressDocument());
		scanner.startASTscan();
		for(ASTNode node : scanner.getMatchedNodes().keySet()) {
			ClosureExpression clExp = ((ClosureExpression)node);

			int posClStart = formatter.getPosOfToken(GroovyTokenTypes.LCURLY,clExp.getLineNumber(),clExp.getColumnNumber(),"{");
			int posCLEnd = formatter.getPosOfToken(GroovyTokenTypes.RCURLY,clExp.getLastLineNumber(),clExp.getLastColumnNumber()-1,"}");

			if(posCLEnd == -1) {
				int positionLastTokenOfClosure = formatter.getPosOfToken(clExp.getLastLineNumber(), clExp.getLastColumnNumber());
				while(formatter.getTokens().get(positionLastTokenOfClosure).getType() != GroovyTokenTypes.RCURLY) {
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
					posParamDelim = formatter.getPosOfNextTokenOfType(posClStart, GroovyTokenTypes.CLOSABLE_BLOCK_OP);
					replaceNLSWithSpace(edits, posClStart, posParamDelim);
				}
				// combine closure with only one statments with less than 5 tokens to one line
				if(codeblock.getStatements().size() == 1 && (posCLEnd - posClStart) < 10) {
                    replaceNLSWithSpace(edits, posParamDelim, posCLEnd);
                    ignoreToken.add(formatter.getTokens().get(posCLEnd));
				} else {
                    // check if there is a linebreak after the parameters
                    if (posParamDelim > 0 && formatter.getNextTokenIncludingNLS(posParamDelim).getType() != GroovyTokenTypes.NLS) {
                        addEdit(new InsertEdit(formatter.getOffsetOfTokenEnd(formatter.getTokens().get(posParamDelim)), formatter
                                .getNewLine()), edits);
                    } else {
                        // If there are no parameters check if the first
                        // statement
                        // is on the next line
                        if (posParamDelim == 0 && formatter.getNextTokenIncludingNLS(posClStart).getType() != GroovyTokenTypes.NLS) {
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
        ReplaceEdit edit = null;
        Token fromToken = null; // remember first NLS token in a string of NLS
                                // tokens
        int p = startPos + 1;
        while (p < endPos) {
            Token token = formatter.getTokens().get(p);
            switch (token.getType()) {
                case GroovyTokenTypes.NLS:
                    if (fromToken == null)
                        fromToken = token;
                    break;
                case GroovyTokenTypes.SL_COMMENT:
                    ++p; // next token will be skipped whether it is a NLS or
                         // not!
                default:
                    if (fromToken != null) {
                        // replace NLS tokens from fromToken up to current token
                        replaceFromTo(fromToken, token, " ", container);
                        fromToken = null;
                    }
                    break;
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

    private void correctBraces(MultiTextEdit edits) throws BadLocationException {
		CorrectLineWrap lCurlyCorrector = null;
		CorrectLineWrap rCurlyCorrector = null;
		if(preferences.getBracesStart() == FormatterPreferences.SAME_LINE)
			lCurlyCorrector = new SameLine(this);
		if(preferences.getBracesStart() == FormatterPreferences.NEXT_LINE)
			lCurlyCorrector = new NextLine(this);
		if(preferences.getBracesEnd() == FormatterPreferences.SAME_LINE)
			rCurlyCorrector = new SameLine(this);
		if(preferences.getBracesEnd() == FormatterPreferences.NEXT_LINE)
			rCurlyCorrector = new NextLine(this);

		assert lCurlyCorrector != null;
		assert rCurlyCorrector != null;

		Token token;
		boolean skipNextNLS = false;
		for (int i = 0; i < formatter.getTokens().size(); i++) {

			token = formatter.getTokens().get(i);
			if(ignoreToken.contains(token))
				continue;

			switch (formatter.getTokens().get(i).getType()) {
				case GroovyTokenTypes.LCURLY:
					if(skipNextNLS){skipNextNLS = false; break;}
					addEdit(lCurlyCorrector.correctLineWrap(i,token),edits);
					break;
				case GroovyTokenTypes.RCURLY:
					if(skipNextNLS){skipNextNLS = false; break;}
					addEdit(rCurlyCorrector.correctLineWrap(i,token),edits);
					break;
				case GroovyTokenTypes.NLS:
					break;
				case GroovyTokenTypes.SL_COMMENT:
					skipNextNLS = true;
			}
		}
	}

	private void addEdit(TextEdit edit,TextEdit container) {
		if(edit != null && edit.getOffset() >= formatter.formatOffset &&
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
			container.addChild(edit);
		}
	}

}
