/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.formatter;

import antlr.Token;
import java.util.Vector;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Mike Klenk mklenk@hsr.ch
 *
 */
public class GroovyLineWrapper {
	
	public DefaultGroovyFormatter formatter;
	private final FormatterPreferences preferences;
	private final LineIndentations lineIndentation;
	MultiTextEdit lineWraps;

	public GroovyLineWrapper(DefaultGroovyFormatter defaultGroovyFormatter,
			FormatterPreferences pref, LineIndentations lineIndentations) {
		this.formatter = defaultGroovyFormatter;
		this.preferences = pref;
		this.lineIndentation = lineIndentations;
		lineWraps = new MultiTextEdit();
	}

	public TextEdit getLineWrapEdits() throws BadLocationException {
		
		Vector<Vector<Token>> tokenLines = formatter.getLineTokens();
		
		for(int line = 0; line < tokenLines.size(); line ++) {
			Token lastTokenOnLine = tokenLines.get(line).get(tokenLines.get(line).size() -1);
			if(lastTokenOnLine.getColumn() >= preferences.maxLineLength) {
				
				int offsetInLine = 0;
				
				while(true) {
					int lastToken = getLastTokenPositionUnderMaximum(tokenLines.get(line),offsetInLine);
					if(lastToken > 0 && lastToken != tokenLines.get(line).size()-2) {
						int replOffset = formatter.getOffsetOfTokenEnd(tokenLines.get(line).get(lastToken));
						int replLength = formatter.getOffsetOfToken(tokenLines.get(line).get(lastToken + 1)) -replOffset;
						String insert = formatter.getNewLine();
						int indentationLevel = lineIndentation.getLineIndentation(lastTokenOnLine.getLine());
						if(!lineIndentation.isMultilineIndentation(lastTokenOnLine.getLine()))
							indentationLevel += preferences.indentationMultiline;
						String leadingGap = formatter.getLeadingGap(indentationLevel);
						lineWraps.addChild(new ReplaceEdit(replOffset,replLength,insert + leadingGap));
						offsetInLine = tokenLines.get(line).get(lastToken + 1).getColumn() - leadingGap.length();
					} else
						break;
				}	
			}
		}
		return lineWraps;
	}

	private int getLastTokenPositionUnderMaximum(Vector<Token> vector, int offsetInLine) throws BadLocationException {
		for(int i = vector.size() -2; i >= 0; i--) {
			Token token = vector.get(i);
			if(token.getColumn() - offsetInLine + formatter.getTokenLength(token) <= preferences.maxLineLength) {
				return i;
			}
		}
		return 0;
	}


}
