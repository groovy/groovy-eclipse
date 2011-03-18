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

import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.greclipse.GroovyTokenTypeBridge;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTNodeInfo;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTScanner;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.IncludesClosureOrListPredicate;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.SourceCodePredicate;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import antlr.Token;

/**
 * @author Mike Klenk mklenk@hsr.ch
 *
 */
public class DefaultGroovyFormatter extends GroovyFormatter {

    private static final boolean DEGUG = false;

    protected IFormatterPreferences pref;
	private ModuleNode rootNode;

    private Document formattedDocument;
	private final boolean indentendOnly;
	public int formatOffset, formatLength;

    private KlenkDocumentScanner tokens;
	private int indentationLevel = 0;

    /**
     * Default Formatter for the Groovy-Eclipse Plugin
     *
     * @param sel The current selection of the Editor
     * @param doc The Document which should be formatted
     * @param map default Plugin preferences, or selfmade preferences
     * @param indentendOnly if true, the code will only be indentended not
     *            formatted
     */
    public DefaultGroovyFormatter(ITextSelection sel, IDocument doc, IFormatterPreferences prefs, boolean indentendOnly) {
        super(sel, doc);
        this.indentendOnly = indentendOnly;
        pref = prefs;
        if (selection.getLength() != 0) {
            try {
                // expand selection to include start of line
                int startLine = doc.getLineOfOffset(selection.getOffset());
                formatOffset = doc.getLineInformation(startLine).getOffset();

                // -1 because we don't want a selection at the start of a new
                // line to cause that line to be formatted
                int endLine = doc.getLineOfOffset(selection.getOffset() + selection.getLength() - 1);
                int end = doc.getLineInformation(endLine).getOffset() + doc.getLineInformation(endLine).getLength();
                formatLength = end - selection.getOffset();
            } catch (BadLocationException e) {
                GroovyCore.logException("Exception when calculating offsets for formatting", e);
                // well, do the best we can
                formatOffset = selection.getOffset();
                formatLength = selection.getLength();
            }

        } else {
            formatOffset = 0;
            formatLength = document.getLength();
        }
    }

    public DefaultGroovyFormatter(IDocument doc, IFormatterPreferences prefs, int indentationLevel) {
        this(new TextSelection(0, 0), doc, prefs, true);
        this.indentationLevel = indentationLevel;
    }

	private void initCodebase() throws Exception {

	    GroovyCore.trace(formattedDocument.get());
        tokens = new KlenkDocumentScanner(formattedDocument);
        rootNode = ASTTools.getASTNodeFromSource(formattedDocument.get());
        if (rootNode == null) {
            // caused by unparseable file
            throw new Exception("Problem parsing Compilation unit.  Fix all syntax errors and try again.");
        }

	}

	/**
     * @param prevToken
     * @param token
     * @return
     */
    private boolean equalTokens(Token t1, Token t2) {
        return t1.getType() == t2.getType() && t1.getColumn() == t2.getColumn() && t1.getLine() == t2.getLine()
               && nullEquals(t1.getFilename(), t2.getFilename()) && nullEquals(t1.getText(), t2.getText());
    }

    /**
     * @param s1
     * @param s2
     * @return
     */
    private boolean nullEquals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        } else if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }

    @Override
	public TextEdit format() {
		formattedDocument = new Document(document.get());
		try {
			if (!indentendOnly) {
				initCodebase();
				GroovyBeautifier beautifier = new GroovyBeautifier(this, pref);
				int lengthBefore = formattedDocument.getLength();
				beautifier.getBeautifiEdits().apply(formattedDocument);
				int lengthAfter = formattedDocument.getLength();
				formatLength += lengthAfter - lengthBefore;
			}

			initCodebase();
			GroovyIndentation indent = new GroovyIndentation(this, pref, indentationLevel );
			UndoEdit undo2 = indent.getIndentationEdits().apply(formattedDocument);
			formatLength += undo2.getLength();


//			if (!indentendOnly) {
//				initCodebase();
//				GroovyLineWrapper linewrap = new GroovyLineWrapper(this, pref, indent.getLineIndentations());
//				UndoEdit undo3 = linewrap.getLineWrapEdits().apply(formattedDocument);
//				formatLength += undo3.getLength();
//			}

		} catch (Exception e) {
            // swallow exception. Caused by unparseable code
            e.printStackTrace();
		}
		if (!formattedDocument.get().equals(document.get()))
			return new ReplaceEdit(0, document.getLength(), formattedDocument
					.get());
        return new MultiTextEdit();
	}

	/**
	 * Searches in the corresponding AST if the given Token is a multiline
	 * statement. Trailling linefeeds and spaces will be ignored.
	 *
	 * @param t
	 *            Token to search for
	 * @return returns true if the statement has more than one line in the
	 *         source code
	 */
	public boolean isMultilineStatement(Token t) {
		if (t != null) {
			ASTNode node = findCorrespondingNode(t);

			if (DEGUG) {
                System.out.println("Searching for: " + t);
                System.out.println(">>>NODE");
                System.out.println(ASTTools.getTextofNode(node, formattedDocument));
                System.out.println("<<<NODE");
			}
			if (isMultilineNodeType(node)) {
				boolean closureTest = false;
                IncludesClosureOrListPredicate cltest = new IncludesClosureOrListPredicate(
						closureTest, t.getLine());
				node.visit(cltest);
				if (!cltest.getContainer()) {
					String text = ASTTools.getTextofNode(node,
							formattedDocument);
					Matcher m = Pattern.compile(".*(\n|\r\n|\r).*",
							Pattern.DOTALL).matcher(trimEnd(text));
					return m.matches();
				}
			}
		}
		return false;
	}

	/**
	 * Returns a string without spaces / line feeds at the end
	 *
	 * @param s
	 * @return
	 */
	public String trimEnd(String s) {
		int len = s.length();

		while (len > 0) {
			String w = s.substring(len - 1, len);
			if (w.matches("\\s"))
				len--;
			else
				break;
		}
		return s.substring(0, len);
	}

	/**
	 * Tests if the ASTNode is a valid MultiNodeType an has multiple lines
	 * Statements, ClassNodes, MethodNodes and Variable Expressions are ignored
	 * @param node
	 * @return
	 */
	private boolean isMultilineNodeType(ASTNode node) {
		if (node != null && node.getLineNumber() < node.getLastLineNumber()) {
			if (node instanceof ExpressionStatement)
				return true;
			if (node instanceof Statement)
				return false;
			if (node instanceof VariableExpression)
				return false;
			if (node instanceof AnnotatedNode)
				return false;

			return true;
		}
		return false;
	}

	/**
	 * Finding a AST Node corresponding to a Token ! Warning ! there can be
	 * found the wrong node if two nodes have the same line / col infos
	 *
	 * @param t
	 *            Token for which the AST Node should be found.
	 * @return the Node with the start position of the token and the longest
	 *         length
	 */
	public ASTNode findCorrespondingNode(Token t) {
		ASTScanner scanner = new ASTScanner(rootNode, new SourceCodePredicate(t
				.getLine(), t.getColumn()), formattedDocument);
		scanner.startASTscan();
		Entry<ASTNode, ASTNodeInfo> found = null;
		if (scanner.hasMatches()) {
			for (Entry<ASTNode, ASTNodeInfo> e : scanner.getMatchedNodes()
					.entrySet()) {
				if (found == null
						|| (found.getValue().getLength() < e.getValue()
								.getLength()))
					found = e;
			}
		}
		if (found != null)
			return found.getKey();
        return null;
	}


	/**
	 * Return a token after many () if there is no opening {
	 *
	 * @param i
	 *            position of the current token
	 * @return the token after the last closing )
	 */
	public Token getTokenAfterParenthesis(int index) {
	    int i = index;
		int countParenthesis = 1;
        while (tokens.get(i).getType() != GroovyTokenTypeBridge.LPAREN) {
			i++;
		}
		i++;
		while (countParenthesis > 0 && i < tokens.size()-1) {
            int ttype = tokens.get(i).getType();
            if (ttype == GroovyTokenTypeBridge.LPAREN) {
					countParenthesis++;
            } else if (ttype == GroovyTokenTypeBridge.RPAREN) {
					countParenthesis--;
			}
			i++;
		}
        if (tokens.get(i).getType() == GroovyTokenTypeBridge.LCURLY ||
		        i >= tokens.size()) {
			return null;
		}
        return getNextToken(i);
	}

	/**
	 * Returns a Sting of spaces / tabs accordung to the configuration
	 *
	 * @param intentation
	 *            the actual indentation level
	 * @return
	 */
	public String getLeadingGap(int indent) {
	    int indentation = indent;
		StringBuilder gap = new StringBuilder();
		while (indentation > 0) {
			if (pref.isUseTabs())
				gap.append("\t");
			else {
				for (int i = 0; i < pref.getTabSize(); i++) {
					gap.append(" ");
				}
			}
			indentation--;
		}
		return gap.toString();
	}

	/**
	 * @return Returns the default newline for this document
	 * @throws BadLocationException
	 */
	public String getNewLine() {
		return formattedDocument.getDefaultLineDelimiter();
	}

	/**
	 * Returns the position of the next token in the collection of parsed tokens
	 *
	 * @param currentPos
	 *            position of the actual cursor
	 * @param includingNLS
	 *            including newline tokens
	 * @return returns the next position in the collection of tokens
	 * or the current position if it is the last token
	 */
	public int getPositionOfNextToken(int cPos, boolean includingNLS) {
	    if (cPos == tokens.size()-1) {
	        return cPos;
	    }
	    int currentPos = cPos;
		int type;
		do {
			type = tokens.get(++currentPos).getType();
        } while ((type == GroovyTokenTypeBridge.WS || (type == GroovyTokenTypeBridge.NLS && !includingNLS))
				&& currentPos < tokens.size() - 2);
		return currentPos;
	}

	public Token getNextToken(int currentPos) {
		return tokens.get(getPositionOfNextToken(currentPos, false));
	}

	public Token getNextTokenIncludingNLS(int currentPos) {
		return tokens.get(getPositionOfNextToken(currentPos, true));
	}

	/**
	 * Returns the position of the previous token in the collection of parsed
	 * tokens
	 *
	 * @param currentPos
	 *            position of the actual cursor
	 * @param includingNLS
	 *            including newline tokens
	 * @return returns the position in the collection of tokens
	 */
	public int getPositionOfPreviousToken(int cPos, boolean includingNLS) {
	    int currentPos = cPos;
		int type;
		do {
			type = tokens.get(--currentPos).getType();
        } while ((type == GroovyTokenTypeBridge.NLS && !includingNLS)
				&& currentPos >= 0);
		return currentPos;
	}

	public Token getPreviousToken(int currentPos) {
		return tokens.get(getPositionOfPreviousToken(currentPos, false));
	}

	public Token getPreviousTokenIncludingNLS(int currentPos) {
		return tokens.get(getPositionOfPreviousToken(currentPos, true));
	}

	/**
	 * Return the offset of a given token in the active document
	 * @param token
	 * @return offset of the token
	 * @throws BadLocationException
	 */
	public int getOffsetOfToken(Token token) throws BadLocationException {
		return formattedDocument.getLineOffset(token.getLine() - 1)
				+ token.getColumn() - 1;
	}

	/**
	 * Return the offset of the end of the token text in the document
	 * @param token
	 * @return the offset in the document after the last character
	 * @throws BadLocationException
	 */
	public int getOffsetOfTokenEnd(Token token) throws BadLocationException {
		int offsetToken = getOffsetOfToken(token);
		int offsetNextToken = getOffsetOfToken(getNextTokenIncludingNLS(getPosOfToken(token)));
		String tokenWithGap = formattedDocument.get(offsetToken,
				offsetNextToken - offsetToken);
		return offsetToken + trimEnd(tokenWithGap).length();
	}

	/**
	 * Counts the length of a Token
	 * @param token
	 * @return length of the token
	 * @throws BadLocationException
	 */
	public int getTokenLength(Token token) throws BadLocationException {
		return getOffsetOfTokenEnd(token) - getOffsetOfToken(token);
	}

	public Vector<Vector<Token>> getLineTokens() {
        return tokens.getLineTokensVector();
	}

    public int getPosOfToken(Token token) throws BadLocationException {
		return tokens.indexOf(token);
	}

	public int getPosOfToken(int tokenType, int line, int column, String tokenText) {
		for(int p = 0; p < tokens.size(); p++) {
			Token a = tokens.get(p);
			if(a.getType() == tokenType &&
					a.getColumn() == column &&
					a.getLine() == line &&
					a.getText().equals(tokenText))
				return p;
		}
		return -1;
	}

	public int getPosOfToken(int lineNumber, int columnNumber) {
		for(int p = 0; p < tokens.size(); p++) {
			Token a = tokens.get(p);
			if(a.getColumn() == columnNumber &&
					a.getLine() == lineNumber)
				return p;
		}
		return -1;
	}

	/**
	 * Get the active state of the document
	 * @return
	 */
	public IDocument getProgressDocument() {
		return formattedDocument;
	}

	public ModuleNode getProgressRootNode() {
		return rootNode;
	}

    public KlenkDocumentScanner getTokens() {
		return tokens;
	}

	/**
     * @param indentationLevel the indentationLevel to set
     */
    public void setIndentationLevel(int indentationLevel) {
        this.indentationLevel = indentationLevel;
    }

	public int getPosOfNextTokenOfType(int pClStart, int expectedType) {
	    int posClStart = pClStart;
		int type;
		do {
			type = tokens.get(++posClStart).getType();
		} while (type != expectedType);
		return posClStart;
	}


	/**
	 * Computes the given indent level for the line by looking at whitespace.
	 * before the line starts.<br><br>
	 * Each tab is consider one indent level.  And for spaces,
	 * the value pref.tabSize is used (rounded up).
	 * @param line the line to look at
	 * @return indentation level
	 */
	public int computeIndentLevel(String line) {
	    int indentsFound = 0;
	    for (int currPos = 0, accumulatedSpaces = 0; currPos < line.length(); currPos++) {
	        char c = line.charAt(currPos);
	        if (c != ' ' && c != '\t') {
	            break;
	        } else if (c == '\t') {
	            indentsFound++;
	            accumulatedSpaces = 0;
	        } else if (c == ' ') {
	            accumulatedSpaces++;
	            if (accumulatedSpaces == pref.getTabSize()) {
	                indentsFound++;
	                accumulatedSpaces = 0;
	            }
	        }
	    }
	    return indentsFound;
	}

}