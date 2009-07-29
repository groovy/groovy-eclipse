/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.formatter;

import antlr.Token;
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
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Mike Klenk mklenk@hsr.ch
 *
 */
public class GroovyBeautifier {
	
	public DefaultGroovyFormatter formatter;
	private final FormatterPreferences preferences;
	private final Set<Token> ignoreToken;


	public GroovyBeautifier(DefaultGroovyFormatter defaultGroovyFormatter,
			FormatterPreferences pref) {
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
					if(posParamDelim > 0 && formatter.getNextTokenIncludingNLS(posParamDelim).getType() != GroovyTokenTypes.NLS) {
						addEdit(new InsertEdit(formatter.getOffsetOfTokenEnd(formatter.getTokens().get(posParamDelim)), formatter.getNewLine()),edits);
					} else { 
						// If there are no parameters check if the first statement is on the next line
						if(posParamDelim == 0 && formatter.getNextTokenIncludingNLS(posClStart).getType() != GroovyTokenTypes.NLS) {
							addEdit(new InsertEdit(formatter.getOffsetOfTokenEnd(formatter.getTokens().get(posClStart)), formatter.getNewLine()),edits);
						}
					}
				}
			}
		}
	}

	private void replaceNLSWithSpace(MultiTextEdit container, int startPos,
			int endPos) throws BadLocationException {
		boolean skipNextNLS = false;
		for(int p = startPos + 1; p < endPos; p++) {
			Token token = formatter.getTokens().get(p);
			if(token.getType() == GroovyTokenTypes.SL_COMMENT) {
				skipNextNLS = true;
			}
			if(token.getType() == GroovyTokenTypes.NLS) {
				if(skipNextNLS) {
					skipNextNLS = false;
				} else {
					int offset = formatter.getOffsetOfToken(token);
					int lenght = formatter.getOffsetOfToken(formatter.getNextToken(p)) - offset;	
					addEdit(new ReplaceEdit(offset,lenght," "), container);
				}
			}
		}
	}

	private void correctBraces(MultiTextEdit edits) throws BadLocationException {
		CorrectLineWrap lCurlyCorrector = null;
		CorrectLineWrap rCurlyCorrector = null;
		if(preferences.bracesStart == FormatterPreferences.SAME_LINE)
			lCurlyCorrector = new SameLine(this);
		if(preferences.bracesStart == FormatterPreferences.NEXT_LINE)
			lCurlyCorrector = new NextLine(this);
		if(preferences.bracesEnd == FormatterPreferences.SAME_LINE)
			rCurlyCorrector = new SameLine(this);
		if(preferences.bracesEnd == FormatterPreferences.NEXT_LINE)
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
			container.addChild(edit);
		}
	}

}
