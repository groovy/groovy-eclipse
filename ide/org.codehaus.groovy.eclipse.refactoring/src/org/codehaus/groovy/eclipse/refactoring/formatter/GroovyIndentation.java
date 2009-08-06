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

import antlr.Token;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Mike Klenk mklenk@hsr.ch
 * 
 */
public class GroovyIndentation {

	private final DefaultGroovyFormatter formatter;
	private final FormatterPreferences pref;

	private int indentation = 0;
	private final int[] tempIndentation;

	private final LineIndentations lineInd;

	TextEdit indentationEdits;

	private final Vector<Token> tokens;

	public GroovyIndentation(DefaultGroovyFormatter formatter,
			FormatterPreferences pref, int indentationLevel) {
		this.formatter = formatter;
		tempIndentation = new int[formatter.getProgressDocument().getNumberOfLines()];
		lineInd = new LineIndentations(formatter.getProgressDocument().getNumberOfLines());

		this.tokens = formatter.getTokens();
		this.pref = pref;
		this.indentation = indentationLevel;
	}

	public TextEdit getIndentationEdits() {
		indentationEdits = new MultiTextEdit();

		try {

			if (formatter.isMultilineStatement(tokens.get(0))) {
				setAdditionalIndentation(tokens.get(0),
						pref.indentationMultiline, false);
				lineInd.setMultilineToken(tokens.get(0).getLine(), tokens.get(0));
			}

			Token token = null;
			for (int i = 0; i < tokens.size() -1 ; i++) {
				token = tokens.get(i);
				int offsetToken = formatter.getOffsetOfToken(token);
				int offsetNextToken = formatter.getOffsetOfToken(formatter
						.getNextTokenIncludingNLS(i));

				
				switch (token.getType()) {
					case GroovyTokenTypes.LITERAL_if:
						setAdditionalIndentation(formatter
								.getTokenAfterParenthesis(i));
						break;
					case GroovyTokenTypes.LITERAL_while:
						setAdditionalIndentation(formatter
								.getTokenAfterParenthesis(i));
						break;
					case GroovyTokenTypes.LITERAL_for:
						setAdditionalIndentation(formatter
								.getTokenAfterParenthesis(i));
						break;
					case GroovyTokenTypes.LCURLY:
						indentation++;
						break;
					case GroovyTokenTypes.LITERAL_switch:
						indentendSwitchStatement(token);
						break;
					case GroovyTokenTypes.RCURLY:
						indentation--;
						break;
					case GroovyTokenTypes.LITERAL_else:
						int nextToken = formatter.getNextToken(i).getType();
						// adding indentation when there is no opening and it is
						// not an "else if" construct
						if (nextToken != GroovyTokenTypes.LCURLY
								&& nextToken != GroovyTokenTypes.LITERAL_if)
							setAdditionalIndentation(formatter.getNextToken(i));
						;
						break;

					case GroovyTokenTypes.NLS:
						if (formatter.getNextTokenIncludingNLS(i).getType() == GroovyTokenTypes.RCURLY)
							tempIndentation[token.getLine()]--;
						int offsetAfterNLS = offsetToken + formatter.getProgressDocument().getLineDelimiter(
								token.getLine()-1).length();
						addEdit(new ReplaceEdit(offsetAfterNLS,
											(offsetNextToken - offsetAfterNLS),
											formatter.getLeadingGap(indentation
																	+ tempIndentation[token.getLine()])));
						lineInd.setLineIndentation(token.getLine()+1, indentation + tempIndentation[token.getLine()]);

						// check if the next token is a multiline Statement
						Token nextMultiToken = formatter.getNextTokenIncludingNLS(i);
						if (formatter.isMultilineStatement(nextMultiToken)) {
							setAdditionalIndentation(nextMultiToken,pref.indentationMultiline, false);
							lineInd.setMultilineToken(token.getLine(), token);
						}

						break;
					case GroovyTokenTypes.ML_COMMENT:
						addEdit(new ReplaceEdit(offsetToken,
										(offsetNextToken - offsetToken),
										formatMultilineComment(
														formatter.getProgressDocument().get(offsetToken,
																		(offsetNextToken - offsetToken)),
														indentation)));
						break;		
				}
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return indentationEdits;
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
	
	private void addEdit(ReplaceEdit edit) {
		if(edit != null && edit.getOffset() >= formatter.formatOffset &&
				edit.getOffset() + edit.getLength() <= formatter.formatOffset + formatter.formatLength) {
			indentationEdits.addChild(edit);
		}
	}

	private void setAdditionalIndentation(Token t, int i,
			boolean firstLineInlcuded) {
		if (t != null) {
			if(t.getType() != GroovyTokenTypes.LCURLY) {
			ASTNode node = formatter.findCorrespondingNode(t);
			if (node != null) {
				int r = node.getLineNumber();
				if (!firstLineInlcuded) {
					r++;
				}
				for (; r <= node.getLastLineNumber(); r++) {
					tempIndentation[r - 1] += i;
					lineInd.setMultilineIndentation(r, true);
				}
			}
			}
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
